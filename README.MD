# AGI Companion Browser

An experimental Android web browser that uses Google's Gemini AI to understand and interact with web pages based on visual context. It captures screenshots, overlays a grid, and allows the AI to perform actions like clicking and typing using grid coordinates (`RxCy`).

This project was developed using **Android AIDE**.

## Key Features

*   **AI-Powered Web Interaction:** Control the browser using natural language prompts via the Gemini AI model (tested with `gemini-1.5-pro-experimental`).
*   **Visual Context Understanding:** The AI uses screenshots (with and without a grid overlay) to perceive the web page state.
*   **Grid-Based Actions:** AI performs actions like `CLICK`, `TYPE`, and `ENTER` on specific page elements identified by grid coordinates (e.g., `R5C8`).
*   **Multi-Turn Sequences:** Engage in complex tasks over multiple interactions, with the AI maintaining context using internal notes and checklists (`NOTE ::` command).
*   **Robust Command Handling:** Parses specific AI commands for navigation, tab management, typing, clicking, and responding.
*   **Standard Browser Functions:** Includes multi-tab support, navigation controls, desktop/mobile user-agent switching, downloads, find-on-page, and local file support (`file:///android_asset/index.html` is the default home/new tab page).
*   **Configurable Grid Overlay:** Toggle a visual grid to understand AI targeting.
*   **API Key Management:** In-app UI to configure your Google Gemini API key.
*   **AI Interaction Log:** Review the sequence of user prompts and AI responses/actions for the current task.
*   **Fullscreen Mode:** Hide controls for an immersive view.

## Setup

1.  **Clone/Download:** Get the project code onto your Android device.
    ```bash
    git clone https://github.com/Gabriel-agi/agi-companion-browser.git
    ```
    (Or download the ZIP from GitHub and extract it).
2.  **Open in AIDE:** Open the extracted project folder (`agi-companion-browser`) in the Android AIDE app.
3.  **Get API Key:** Follow the steps below to obtain and configure your Google Gemini API key. **The AI features will not work without it.**
4.  **Build & Run:** Use AIDE's build (`Hammer icon` or `Menu > Build/Run > Run`) and run options to install the app on your device.

## Getting Your Gemini API Key

This app requires a Google Gemini API key to function. Google currently offers a free tier suitable for testing and development.

1.  Visit the **[Google AI Studio](https://aistudio.google.com/)**.
2.  Sign in with your Google account.
3.  Click on "**Get API key**" (usually on the left sidebar).
4.  Click "**Create API key in new project**" (or select an existing project).
5.  **Copy the generated API key.** Treat this key like a password and do not share it publicly!
6.  Launch the AGI Companion Browser app on your device.
7.  Open the **Menu** (three vertical dots icon in the top bar).
8.  Select "**API Key Settings**".
9.  Enter the key using the format `1,YOUR_API_KEY` (where `YOUR_API_KEY` is the key you just copied) and tap "**Save**".
    *   *(The number `1` is just an index; you can add more keys later if needed, like `1,keyA,2,keyB`, and the app will use the key associated with the highest index number entered).*

## Usage

1.  Navigate to a web page using the URL bar or open a local file via the Menu.
2.  Tap the **Grid** icon (looks like a phone dialer) to optionally toggle the visual grid overlay. This helps visualize where the AI might click. You can set the density (number of lines) when enabling it.
3.  Tap the **AI** icon (looks like a paper airplane/send icon) to start an AI interaction sequence.
4.  Enter your prompt describing the task you want the AI to perform (e.g., "Click the login button", "Find the search bar, type 'Android AIDE', and press Enter").
5.  The app will capture the screen (with and without the grid), send it with your prompt and other context (like open tabs) to the Gemini AI.
6.  The AI will respond with commands (like `CLICK R3C4`, `TYPE R5C2 :: myUsername`, `NAVIGATE https://...`, or `RESPOND Your final answer...`). The app executes these commands.
7.  The AI interaction continues turn-by-turn, capturing new context after each action, until the task is complete, it uses the `RESPOND` command, or the maximum number of turns is reached.
8.  Tap the **Log** icon (looks like a clock/history icon) during or after a sequence to view the detailed interaction (your prompts, AI actions, AI internal notes/checklists) for that sequence.
9.  Use the **Menu** (three dots icon) for other options like toggling Desktop/Mobile mode, managing API keys, viewing downloads, etc.

## Contributing

Contributions, issues, and feature requests are welcome! Please feel free to submit pull requests or open issues on the GitHub repository.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
