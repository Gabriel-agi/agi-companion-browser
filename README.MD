[English Version](README.en.md) | Versão em Inglês

---

# AGI Companion Browser

**Controle a web visualmente com o poder da IA Gemini! (Outros modelos multimodais serão adicionados em updates futuros, i.e Qwen, DeepSeek, Llama, etc.)**

O AGI Companion Browser é um navegador experimental para Android que funciona como um **Agente de IA**. Ele permite que uma inteligência artificial (Google Gemini) compreenda e interaja com páginas web baseadas em capturas de tela e comandos direcionados a uma grade visual sobreposta.

## Recursos Principais

*   **Agente de IA Integrado:** Controle o navegador usando prompts em linguagem natural via Gemini AI.
*   **Compreensão Visual:** A IA usa capturas de tela (com e sem grade) para "ver" e entender o estado da página web.
*   **Ações Baseadas em Grade:** Execute ações como `CLICK` (clicar), `TYPE` (digitar) e `ENTER` em elementos específicos usando coordenadas da grade (ex: `R5C8`).
*   **Sequências Multi-Turno:** Realize tarefas complexas através de múltiplas interações, com a IA mantendo o contexto via notas internas e checklists (`NOTE ::`).
*   **Processamento de Comandos:** Interpreta comandos específicos da IA para navegação, gerenciamento de abas, digitação, cliques e respostas finais.
*   **Navegador Completo:** Inclui abas múltiplas, controles de navegação, modo Desktop/Mobile, downloads, busca na página e suporte a arquivos locais (página inicial padrão em `file:///android_asset/index.html`).
*   **Grade Visual Configurável:** Ative uma grade visual para entender e direcionar as ações da IA.
*   **Gerenciamento de Chave de API:** Interface no app para configurar sua chave de API do Google Gemini.
*   **Log de Interação:** Revise a sequência de prompts do usuário e respostas/ações da IA.
*   **Modo Tela Cheia:** Oculte os controles para uma visualização imersiva.

## Configuração

1.  **Clone/Baixe:** Obtenha o código do projeto no seu dispositivo.
    ```bash
    git clone https://github.com/Gabriel-agi/agi-companion-browser.git
    ```
    (Ou baixe o ZIP do GitHub e extraia).
2.  **Chave de API:** Obtenha e configure sua chave de API do Google Gemini (instruções abaixo). **As funções de IA não funcionarão sem ela.**
3.  **Instalação:** Compile o projeto e instale o APK gerado no seu dispositivo Android.

## Obtendo sua Chave de API Gemini

Este app requer uma chave de API do Google Gemini. O Google oferece um nível gratuito adequado para testes.

1.  Visite o **[Google AI Studio](https://aistudio.google.com/)**.
2.  Faça login com sua conta Google.
3.  Clique em "**Get API key**" (Obter chave de API), geralmente na barra lateral esquerda.
4.  Clique em "**Create API key in new project**" (Criar chave em novo projeto) ou selecione um existente.
5.  **Copie a chave de API gerada.** Trate-a como uma senha!
6.  Abra o app AGI Companion Browser.
7.  Abra o **Menu** (ícone de três pontos).
8.  Selecione "**API Key Settings**".
9.  Insira a chave no formato `1,SUA_CHAVE_API` (substitua `SUA_CHAVE_API` pela chave copiada) e toque em "**Save**".
    *   *(O número `1` é um índice; você pode adicionar mais chaves como `1,keyA,2,keyB`, e o app usará a chave do maior índice).*

## Como Usar

1.  Navegue até uma página usando a barra de URL ou abra um arquivo local pelo Menu.
2.  Toque no ícone **Grade** (parece um teclado numérico) para ativar/desativar a grade visual (útil para entender o alvo da IA).
3.  Toque no ícone **IA** (avião de papel/enviar) para iniciar uma sequência de interação.
4.  Digite seu prompt descrevendo a tarefa (ex: "Clique no botão de login", "Ache a barra de busca, digite 'Android AI' e aperte Enter").
5.  O app captura a tela, envia para a IA Gemini com seu prompt e contexto (abas abertas, etc.).
6.  A IA responde com comandos (`CLICK R3C4`, `TYPE R5C2 :: meuUsuario`, `NAVIGATE https://...`, `RESPOND Sua resposta final...`), e o app os executa.
7.  A interação continua turno a turno, capturando novo contexto após cada ação, até a tarefa ser concluída, a IA usar `RESPOND`, ou o limite de turnos ser atingido.
8.  Toque no ícone **Log** (relógio/histórico) para ver o histórico detalhado da interação atual.
9.  Use o **Menu** (três pontos) para outras opções (Modo Desktop/Mobile, Chaves de API, Downloads, etc.).

## Contribuições

Contribuições, issues e sugestões são bem-vindos! Sinta-se à vontade para abrir pull requests ou issues no repositório GitHub.

## Licença

Este projeto é licenciado sob a **Licença MIT** - veja o arquivo [LICENSE](LICENSE) para detalhes.
