# 📋 Plano de Implementação — Issue #49: Estabilização do Leitor de Código de Barras

> **Issue:** [#49 — [Bug]: Leitor de código de barras faz leitura prematura e instável antes do enquadramento central](https://github.com/bitlabbr/Minha-Despensa/issues/49)  
> **Branch de Trabalho:** `fix/49-barcode-scanner-stabilizatio`  
> **Data:** Outubro de 2026  
> **Status:** Em Planejamento / Execução

---

## 🎯 Objetivo

Eliminar o disparo prematuro, acidental ou truncado de códigos de barras ao abrir o scanner da câmera, garantindo que a captura só seja confirmada quando o código estiver:
1. **Centralizado na mira visual (Region of Interest - ROI)** exibida na tela.
2. **Estável e em foco**, confirmado por detecção idêntica em múltiplos frames consecutivos (*debounce* temporal).

---

## 🔍 Diagnóstico do Problema Atual

Atualmente, em `BarcodeScannerCameraView.android.kt`:
1. **Ausência de validação de coordenadas:** O analisador do ML Kit processa todo o sensor da câmera (1080p/4K). Se qualquer barra for identificada na borda extrema da imagem, o código é considerado lido.
2. **Disparo no primeiro frame:** Ao detectar a primeira string (`rawValue`), o callback `onBarcodeScanned(rawValue)` é acionado imediatamente sem qualquer confirmação de foco ou estabilização.
3. **Leituras parciais/truncadas:** Produtos em movimento rápido geram leituras incompletas (ex: 8 dígitos ao invés de 13 dígitos do EAN-13) antes de a câmera travar o autofoco.

---

## 🏗️ Arquitetura da Solução

```text
┌───────────────────────────────────────────────────────────────┐
│ CameraX Frame (1080x1920)                                      │
│                                                               │
│   ┌───────────────────────────────────────────────────────┐   │
│   │ Central Region of Interest (ROI: 60% w x 40% h)       │   │
│   │                                                       │   │
│   │     ┌───────────────┐                                 │   │
│   │     │  Barcode Box  │ ──► [BarcodeRoiValidator]       │   │
│   │     └───────────────┘           │                     │   │
│   └─────────────────────────────────┼─────────────────────┘   │
│                                     ▼ (Dentro da ROI)         │
│                        [BarcodeScanStabilizer]                │
│                        (2 frames consecutivos)                │
│                                     │                         │
│                                     ▼ (Estável & Confirmado)  │
│                       onBarcodeScanned(code)                  │
└───────────────────────────────────────────────────────────────┘
```

1. **`BarcodeRoiValidator` (Comum / Testável):**
   * Valida se o centro geométrico da caixa delimitadora (`boundingBox`) do código de barras está dentro dos limites da mira central percentual.
   * Suporta orientações de sensor de 0°, 90°, 180° e 270°.

2. **`BarcodeScanStabilizer` (Comum / Testável):**
   * Máquina de estados temporal em Kotlin puro.
   * Exige que o mesmo código de barras seja lido em `N` frames consecutivos (padrão: 2 frames) dentro de um intervalo máximo (padrão: 400ms).
   * Caso o código mude (leitura parcial seguida da completa) ou demore mais que o intervalo, o contador reinicia.

3. **`BarcodeScannerCameraView.android.kt`:**
   * Integra o validador de ROI e o estabilizador dentro do `ImageAnalysis.Analyzer`.

4. **`BarcodeScannerModal.kt`:**
   * Alinha as proporções da mira visual (`Box` com bordas) à ROI computacional.
   * Feedback háptico acionado apenas quando a leitura é confirmada pelo estabilizador.

---

## 🪜 Etapas Sub-atômicas e Commits Planejados

Cada etapa é estritamente isolada, compilável, testável e acompanhada de seu próprio commit convencional.

| Etapa | Escopo | Ação Detalhada | Mensagem do Commit |
| :---: | :--- | :--- | :--- |
| **1** | `uisystem` (commonMain) | Criar o componente puro `BarcodeScanStabilizer` com lógica de contagem consecutiva e tolerância de tempo. | `feat(scanner): add BarcodeScanStabilizer for multi-frame debounce and confirmation` |
| **2** | `uisystem` (commonTest) | Criar a suíte de testes unitários `BarcodeScanStabilizerTest` cobrindo cenários de sucesso, reset por código diferente, timeout e edge cases. | `test(scanner): add unit tests for BarcodeScanStabilizer state transitions` |
| **3** | `uisystem` (commonMain) | Criar o validador geométrico `BarcodeRoiValidator` com cálculo de percentual de área central e suporte a rotações do sensor. | `feat(scanner): add BarcodeRoiValidator for central reticle bounding box filtering` |
| **4** | `uisystem` (commonTest) | Criar a suíte de testes unitários `BarcodeRoiValidatorTest` validando coordenadas centrais, extremidades, limites e rotações de tela. | `test(scanner): add unit tests for BarcodeRoiValidator bounding box geometry` |
| **5** | `uisystem` (androidMain) | Integrar `BarcodeRoiValidator` e `BarcodeScanStabilizer` no analisador da CameraX (`BarcodeScannerCameraView.android.kt`). | `fix(scanner): integrate ROI filtering and stabilizer into Android CameraX analyzer` |
| **6** | `uisystem` (commonMain) | Alinhar visualmente a caixa de mira do `BarcodeScannerModal.kt` com a proporção da ROI e refinar feedback de sucesso. | `feat(scanner): refine reticle alignment and visual feedback in BarcodeScannerModal` |
| **7** | Global | Executar suíte completa de testes (`./gradlew testDebugUnitTest`), validar ausência de regressões e verificar build final. | `chore(scanner): verify test suite and clean up scanner implementation` |

---

## 📊 Critérios de Aceite (Definition of Done)

- [ ] Nenhum código fora da mira central da tela é aceito ou processado.
- [ ] O leitor exige ao menos 2 frames consecutivos idênticos antes de emitir a confirmação.
- [ ] Leituras transitórias/borradas em movimento rápido não causam captura de dados truncados.
- [ ] 100% dos novos testes unitários passam de forma determinística em `uisystem`.
- [ ] Nenhuma regressão na suíte de testes existente do projeto.
