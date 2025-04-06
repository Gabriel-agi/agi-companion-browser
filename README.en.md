[Versão em Português](README.md) | [简体中文版](README.zh-CN.md) | Portuguese Version | Simplified Chinese Version

---

# AGI Companion Browser

**An AI Agent Browser for Android: Control the web visually!**

AGI Companion Browser transforms web browsing into an interactive experience controlled by AI. It functions as an **AI Agent** that allows advanced multimodal models to understand web pages through screenshots and execute precise actions using an overlaid visual grid.

Currently integrated with **Google Gemini**, with plans to support other powerful models like **Qwen**, **DeepSeek**, and **Llama** in future updates!

## Key Features

*   **Autonomous Browsing:** Give natural language commands for the AI to navigate, click, type, and interact with websites.
*   **Visual Understanding:** The AI uses screenshots (with and without a grid) to "see" and understand page content and layout.
*   **Precise Grid-Based Actions:** Execute actions (`CLICK`, `TYPE`, `ENTER`) on specific elements using grid coordinates (e.g., `R5C8`).
*   **Complex Tasks (Multi-Turn):** Perform action sequences over multiple interactions, with the AI maintaining context via internal notes and checklists (`NOTE ::`).
*   **Multi-Model Support (Planned):** Designed to integrate various multimodal models (Qwen, DeepSeek, Llama coming soon!).
*   **Full Browser Features:** Includes multiple tabs, Desktop/Mobile modes, downloads, find-on-page, etc.
*   **Configurable Visual Grid:** Enable a grid to target and understand AI actions.
*   **API Key Management:** Interface to configure your required API keys.
*   **Interaction Log:** Review the complete communication between you and the AI agent.

## Setup

1.  **Clone/Download:** Get the project code.
    ```bash
    git clone https://github.com/Gabriel-agi/agi-companion-browser.git
    ```
2.  **API Key:** Configure the API key for the currently supported model (Gemini). See instructions below. **The AI will not function without it.**
3.  **Installation:** Build the project and install the generated APK on your Android device.

## Getting Your API Key (Example: Gemini)

The app currently uses Gemini and requires an API key. Similar processes will be needed for future models.

1.  Visit the **[Google AI Studio](https://aistudio.google.com/)** (for Gemini).
2.  Sign in and click "**Get API key**".
3.  Click "**Create API key...**".
4.  **Copy the generated key.**
5.  In the AGI Companion Browser app > **Menu** > **API Key Settings**.
6.  Enter in the format `1,YOUR_API_KEY` and tap "**Save**".

## Usage

1.  Navigate to a web page.
2.  Tap the **Grid** icon to toggle the visual grid.
3.  Tap the **AI** icon to start an interaction.
4.  Enter your command (e.g., "Click the 'Confirm' button", "Type 'Multimodal AI' in search bar R3C5 and press Enter").
5.  The app captures the screen, sends it to the AI, which responds with actions.
6.  The interaction continues turn-by-turn until the task is complete.
7.  Use the **Log** icon to see the interaction history.

## Contributing

Contributions, issues, and feature requests are welcome!

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
