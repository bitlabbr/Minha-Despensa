# 📦 Release Notes — Minha Despensa v1.2.1-alpha

> **Versão:** `v1.2.1-alpha`  
> **Build (VersionCode):** `5`  
> **Data de Lançamento:** 29 de Outubro de 2026  
> **Plataformas:** Android (APK In-App OTA) & iOS (Compose Multiplatform)  
> **Repositório:** [bitlabbr/Minha-Dispensa](https://github.com/bitlabbr/Minha-Dispensa)

---

## 🎯 Destaques do Lançamento

A versão **v1.2.1-alpha** traz um canal completo de **Suporte e Diagnóstico In-App**, recuperação inteligente de falhas (crashes de sessões anteriores), exportação nativa de logs do dispositivo e melhorias críticas de conectividade e TLS para o cliente iOS.

---

## 🚀 O Que Há de Novo

### 🐞 1. Suporte e Relato de Bugs In-App (`PR #89`)
- **Seção de Suporte na Conta:** Nova seção dedicada em *Configurações da Conta* com botão de ação direta *"Relatar Problema ou Sugestão"*.
- **Diálogo Intuitivo de Envio:** Classificação em categorias claras:
  - 🐛 **Bug / Falha**: Problemas de execução, tela ou sincronização.
  - 💡 **Sugestão**: Novas ideias de recursos para o app.
  - ❓ **Dúvida**: Perguntas de uso ou operação.
  - 💥 **Crash**: Falha crítica identificada.
- **Protocolo de Atendimento:** Geração imediata de protocolo rastreável (ex: `BUG-20261029-A1B2`) exibido em banner de confirmação.

### 📝 2. Diagnósticos e Buffer Circular de Logs Sanitizados
- **`RingBufferLogRecorder` (1500 entradas):** Gravação contínua em memória dos logs da aplicação com sanitização automática de credenciais e dados sensíveis via `LogSanitizer`.
- **Anexo Automático de Logs:** Opção do usuário anexar o arquivo de log (`device.log`) diretamente na submissão do chamado.
- **Exportação via OS Share Sheet:** Opção de exportar e compartilhar os logs de diagnóstico diretamente através de aplicativos externos (arquivos, mensageiros, email) em plataformas Android e iOS.

### 💥 3. Recuperação e Notificação de Crashes
- **`UncaughtExceptionHandler` Global:** Captura falhas não tratadas na aplicação e persiste o stack trace localmente em `pending_crash.txt`.
- **Banner Proativo na Inicialização:** Na inicialização seguinte após uma interrupção inesperada, o usuário é avisado com opção de enviar o relato da falha com um único toque.

### 🍏 4. Resiliência de Rede & TLS no iOS (`PR #90`)
- **Darwin Engine TLS Trust:** Tratamento adequado de desafios TLS no motor Ktor Darwin para ambientes internos e self-hosted (`despensa.domus.arpa`).
- **Refatoração de Provedores Nativos:** Reorganização do pacote de provedores de plataforma (`provider`), garantindo paridade total entre Android e iOS.

---

## 🔧 Correções e Melhorias Internas

- **Higiene de Strings:** Extração de strings hardcoded de recibos de suporte e mensagens para recursos tipados `UiText` em `:uisystem`.
- **Eager Initialization:** Inicialização imediata do manipulador global de falhas no `onCreate` do `Application` Android.
- **Incremento de Build:** Atualizado `versionCode = 5` e `versionName = "1.2.1-alpha"`.

---

## 📲 Como Atualizar

* **No Dispositivo Android:** Acesse a tela de **Conta** (`HouseholdAccountScreen`) ➔ Seção **Atualização de Software** ➔ Clique em **"Verificar Atualizações"** ➔ **"Baixar e Instalar"**.
* **Instalação Manual:** Faça download do binário `minha-despensa-1.2.1-alpha.apk` no servidor oficial [https://despensa.domus.arpa](https://despensa.domus.arpa).
