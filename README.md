[English Version](README.en.md) | [简体中文版](README.zh-CN.md) | Versão em Inglês | Versão em Chinês Simplificado

---

# AGI Companion Browser

**Um Navegador Agente de IA para Android: Controle a web visualmente!**

O AGI Companion Browser transforma a navegação web em uma experiência interativa controlada por IA. Ele funciona como um **Agente de IA** que permite a modelos multimodais avançados compreenderem páginas web através de capturas de tela e executarem ações precisas usando uma grade visual sobreposta.

Atualmente integrado com **Google Gemini**, com planos para suportar outros modelos poderosos como **Qwen**, **DeepSeek** e **Llama** em futuras atualizações!

## Recursos Principais

*   **Navegação Autônoma:** Dê comandos em linguagem natural para que a IA navegue, clique, digite e interaja com websites.
*   **Compreensão Visual:** A IA usa capturas de tela (com e sem grade) para "ver" e entender o conteúdo e layout da página.
*   **Ações Precisas Baseadas em Grade:** Execute ações (`CLICK`, `TYPE`, `ENTER`) em elementos específicos usando coordenadas da grade (ex: `R5C8`).
*   **Tarefas Complexas (Multi-Turno):** Realize sequências de ações através de múltiplas interações, com a IA mantendo o contexto via notas internas e checklists (`NOTE ::`).
*   **Suporte a Múltiplos Modelos (Planejado):** Projetado para integrar diversos modelos multimodais (Qwen, DeepSeek, Llama em breve!).
*   **Navegador Completo:** Inclui abas múltiplas, modo Desktop/Mobile, downloads, busca na página, etc.
*   **Grade Visual Configurável:** Ative uma grade para direcionar e entender as ações da IA.
*   **Gerenciamento de Chave de API:** Interface para configurar suas chaves de API necessárias.
*   **Log de Interação:** Revise a comunicação completa entre você e o agente de IA.

## Configuração

1.  **Clone/Baixe:** Obtenha o código do projeto.
    ```bash
    git clone https://github.com/Gabriel-agi/agi-companion-browser.git
    ```
2.  **Chave de API:** Configure a chave de API para o modelo atualmente suportado (Gemini). Veja instruções abaixo. **Sem a chave, a IA não funciona.**
3.  **Instalação:** Compile o projeto e instale o APK no seu dispositivo Android.

## Obtendo sua Chave de API (Exemplo: Gemini)

O app atualmente usa Gemini e requer uma chave de API. Processos similares serão necessários para futuros modelos.

1.  Visite o **[Google AI Studio](https://aistudio.google.com/)** (para Gemini).
2.  Faça login e clique em "**Get API key**".
3.  Clique em "**Create API key...**".
4.  **Copie a chave gerada.**
5.  No app AGI Companion Browser > **Menu** > **API Key Settings**.
6.  Insira no formato `1,SUA_CHAVE_API` e toque em "**Save**".

## Como Usar

1.  Navegue até uma página web.
2.  Toque no ícone **Grade** para ativar/desativar a grade visual.
3.  Toque no ícone **IA** para iniciar uma interação.
4.  Digite seu comando (ex: "Clique no botão 'Confirmar'", "Digite 'IA multimodal' na busca R3C5 e aperte Enter").
5.  O app captura a tela, envia para a IA, que responde com ações.
6.  A interação continua turno a turno até a tarefa ser concluída.
7.  Use o ícone **Log** para ver o histórico da interação.

## Contribuições

Contribuições, issues e sugestões são bem-vindos!

## Licença

Este projeto é licenciado sob a **Licença MIT** - veja o arquivo [LICENSE](LICENSE) para detalhes.
