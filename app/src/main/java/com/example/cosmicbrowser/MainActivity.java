package com.example.cosmicbrowser;

// Imports (No changes needed)
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.SocketTimeoutException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Complete MainActivity.java for Cosmic Browser with AI Agent enhancements.
 * - Gemini AI integration.
 * - Grid overlay.
 * - Multi-turn AI sequences with Action + NOTE on Turn 1.
 * - AI Interaction Log.
 * - **Extremely verbose internal AI notes (`NOTE ::`) mandated via prompt.**
 * - Tab management, navigation, desktop/mobile mode, etc.
 * - API Key management.
 * - Robust command parsing.
 */
public class MainActivity extends Activity {

    // Member variables (No changes needed)
    private FrameLayout rootLayout;
    private LinearLayout controls;
    private EditText urlBar;
    private FrameLayout webViewContainer;
    private HorizontalScrollView tabScroll;
    private LinearLayout tabLayout;
    private View touchZone;
    private View accentLine;
    private List<Tab> tabs = new ArrayList<>();
    private Tab currentTab;
    private List<String> downloadHistory = new ArrayList<>();
    private boolean isDesktopMode = false;
    private boolean isFullscreen = false;
    private int controlsHeight = 0;
    private GridView gridOverlayView;
    private boolean isGridVisible = false;
    private int currentGridDensity = 10;
    private ImageButton aiButtonControl;
    private boolean isAiSequenceRunning = false;
    private int currentAiTurn = 0;
    private String aiInternalNote = "";
    private List<String> aiInteractionLog = new ArrayList<>();
    private Handler uiHandler;
    private SharedPreferences sharedPreferences;

    // Constants (No changes needed)
    private static final int MAX_AI_TURNS = 9;
    private static final String SEARCH_ENGINE = "https://www.google.com/search?q=";
    private static final String MOBILE_AGENT = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36";
    private static final String DESKTOP_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro-exp-03-25:generateContent?key=";
    private static final String CMD_CLICK = "CLICK ";
    private static final String CMD_TYPE = "TYPE ";
    private static final String CMD_NAVIGATE = "NAVIGATE ";
    private static final String CMD_RESPOND = "RESPOND ";
    private static final String CMD_ENTER = "ENTER ";
    private static final String CMD_NEW_TAB = "NEW_TAB";
    private static final String CMD_NOTE = "NOTE ";
    private static final String CMD_SEPARATOR = " :: ";
    private static final String TAG = "CosmicBrowserAI";
    private static final String PREFS_NAME = "CosmicBrowserPrefs";
    private static final String API_KEYS_PREF = "apiKeysString";
    private static final String SELECTED_API_KEY_INDEX_PREF = "selectedApiKeyIndex";
    // *** CHANGE 1 of 3: Define the asset home page URL ***
    private static final String HOME_PAGE_URL = "file:///android_asset/index.html";


    // *** UPDATED: System Prompt demanding verbose notes and Turn 1 action ***
    private static final String SYSTEM_PROMPT =
            "---\n" +
            "SYSTEM INSTRUCTIONS:\n" +
            "You are an AI assistant controlling a web browser view.\n" +
            "You receive TWO primary visual inputs (Clean & Grid Screenshots) and Text inputs (Tab List, Previous AI Note).\n" +
            "**CRITICAL: Your internal `NOTE` command content is your memory. Make it EXTREMELY DETAILED (hundreds or thousands of lines if necessary). API calls are charged per-request, not by token count, so be verbose!**\n\n" +
            "**RESPONSE FORMAT RULES:**\n" +
            "1.  **TURN 1 (User Prompt Received):**\n" +
            "    a. Perform ONE action command (e.g., `CLICK R1C1`, `NAVIGATE https://...`) based on the user request, on the *first line BY ITSELF*.\n" +
            "    b. Add a *newline* character (`\\n`).\n" +
            "    c. On the *next line*, start the `NOTE :: <Initial Analysis, Reasoning, and Checklist>` command. This initial note MUST include:\n" +
            "        *   `**User Request Summary:**` Brief re-statement of the user's goal.\n" +
            "        *   `**Reasoning for First Action:**` Why did you choose the specific action you took on line 1?\n" +
            "        *   `**Initial Plan (Checklist):**` Detailed, multi-step markdown checklist for the *entire* task (e.g., `- [ ] Step 1: Do X`). Mark the first action (from line 1) as completed if applicable (e.g., `- [x] Navigate to Y`).\n" +
            "        *   `**Initial Visual Analysis:**` Describe the *initial* state based on the provided screenshots.\n" +
            "2.  **SUBSEQUENT TURNS (Turn 2+):**\n" +
            "    a. Output ONE action command on the *first line BY ITSELF*.\n" +
            "    b. Add a *newline* character (`\\n`).\n" +
            "    c. On the *next line*, start `NOTE :: <Extremely Detailed Update>`. This note MUST include:\n" +
            "        *   `**Current Goal:**` What specific sub-task from the checklist is this turn trying to achieve?\n" +
            "        *   `**Visual Analysis:**` VERY DETAILED description of BOTH screenshots (Clean/Grid). What changed? What is visible? What is the current state? Describe elements near relevant grid cells.\n" +
            "        *   `**Previous Action Outcome:**` What was the *intended* result of the *last* action? What *actually* happened based on the *new* screenshots? Did it work? Why or why not? (Analyze potential errors).\n" +
            "        *   `**Reasoning for Next Action:**` Based on Goal, Visual Analysis, and Previous Outcome, why is the chosen action (on line 1 of this response) the best next step? Justify your choice thoroughly.\n" +
            "        *   `**Updated Checklist:**` Copy the *entire* checklist from the *previous* NOTE. Mark the step corresponding to the action on line 1 of *this* response as completed (`[x]`). Add/Modify/Remove future steps if necessary based on your reasoning and observations. Explain any changes to the plan.\n" +
            "3.  **FINAL TURN:** When the task is complete (all checklist items done or goal met), output *only* the `RESPOND <Your natural language answer>` command.\n" +
            "4.  **Formatting:** Do NOT add quotes (' or \") or backticks (`) around commands. Use markdown formatting within the NOTE where appropriate (like checklists and bold section headers).\n\n" +
            "**AVAILABLE ACTIONS (Choose ONE per turn, follow formatting rules):**\n" +
            "*   `CLICK RxCy`\n" +
            "*   `TYPE RxCy :: Text to Type`\n" +
            "*   `ENTER RxCy`\n" +
            "*   `NAVIGATE URL`\n" +
            "*   `NEW_TAB [Optional URL]`\n" +
            "*   `NOTE :: <Detailed message/analysis/checklist>` (REQUIRED on ALL turns except the final RESPOND turn.)\n" +
            "*   `RESPOND Your answer here` (ONLY for final answer. ENDS sequence.)\n\n" +
            "**Remember:** Maximum detail in the NOTE is crucial for maintaining context and achieving the goal effectively. Analyze everything.\n" +
            "---";

    // --- Custom Grid View with Labels ---
    private static class GridView extends View {
        private Paint linePaint = new Paint(); private Paint textPaint = new Paint(); private int density = 10; private float cellWidth = 0; private float cellHeight = 0;
        public GridView(Context c) { super(c); init(); }
        private void init() { linePaint.setColor(0xAA00FF00); linePaint.setStrokeWidth(1f); linePaint.setStyle(Paint.Style.STROKE); textPaint.setColor(Color.BLACK); textPaint.setTextSize(40f); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setAntiAlias(true); textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); textPaint.setShadowLayer(2f, 1f, 1f, 0xAAFFFFFF); }
        public void setGridDensity(int d) { density = Math.max(1, Math.min(d, 50)); updateCellDimensions(); invalidate(); }
        private void updateCellDimensions() { int w = getWidth(); int h = getHeight(); if (density <= 0 || w <= 0 || h <= 0) { cellWidth = 0; cellHeight = 0; } else { cellWidth = (float) w / (density + 1); cellHeight = (float) h / (density + 1); } }
        @Override protected void onSizeChanged(int w, int h, int ow, int oh) { super.onSizeChanged(w, h, ow, oh); updateCellDimensions(); }
        @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas); int w = getWidth(); int h = getHeight(); if (density <= 0 || w <= 0 || h <= 0 || cellWidth <= 0 || cellHeight <= 0) return; for (int i = 1; i <= density; i++) canvas.drawLine(0, i * cellHeight, w, i * cellHeight, linePaint); for (int j = 1; j <= density; j++) canvas.drawLine(j * cellWidth, 0, j * cellWidth, h, linePaint); for (int r = 0; r <= density; r++) { for (int c = 0; c <= density; c++) { String label = "R" + (r + 1) + "C" + (c + 1); float centerX = c * cellWidth + cellWidth / 2; float centerY = r * cellHeight + cellHeight / 2; float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2); canvas.drawText(label, centerX, textY, textPaint); } } }
        public float[] getCenterCoordinatesForLabel(String label) { if (label == null || !label.matches("R\\d+C\\d+") || cellWidth <= 0 || cellHeight <= 0) { Log.w(TAG, "Invalid label or grid not ready for coordinate calculation: " + label); return null; } try { String[] parts = label.substring(1).split("C"); int row = Integer.parseInt(parts[0]) - 1; int col = Integer.parseInt(parts[1]) - 1; if (row < 0 || row > density || col < 0 || col > density) { Log.w(TAG, "Label indices out of bounds: " + label + " (density=" + density + ")"); return null; } float centerX = col * cellWidth + cellWidth / 2; float centerY = row * cellHeight + cellHeight / 2; return new float[]{centerX, centerY}; } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { Log.e(TAG, "Error parsing grid label: " + label, e); return null; } }
    }

    // --- Tab Class ---
     private class Tab {
        WebView webView; LinearLayout tabView; TextView titleView; ImageButton closeButton; String title = "New Tab"; String url = ""; String initialUrl = "";
        Tab() { setupWebView(); setupTabView(); }
        private void setupWebView() { webView = new WebView(MainActivity.this); WebSettings s = webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setCacheMode(WebSettings.LOAD_DEFAULT); s.setGeolocationEnabled(true); s.setSaveFormData(true); s.setSavePassword(false); s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setAllowFileAccessFromFileURLs(true); s.setAllowUniversalAccessFromFileURLs(true); s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE); s.setSupportZoom(true); s.setBuiltInZoomControls(true); s.setDisplayZoomControls(false); s.setUseWideViewPort(true); s.setLoadWithOverviewMode(true); s.setUserAgentString(isDesktopMode ? DESKTOP_AGENT : MOBILE_AGENT); CookieManager cm = CookieManager.getInstance(); cm.setAcceptCookie(true); if (android.os.Build.VERSION.SDK_INT >= 21) cm.setAcceptThirdPartyCookies(webView, true); webView.setWebViewClient(new WebViewClient() { @Override public void onPageStarted(WebView v, String u, Bitmap f) { if (currentTab == Tab.this) urlBar.setText(u); Tab.this.url = u; if (Tab.this.initialUrl.isEmpty() || Tab.this.initialUrl.startsWith("about:")) Tab.this.initialUrl = u; MainActivity.this.updateNavigationButtons(); } @Override public void onPageFinished(WebView v, String u) { if (currentTab == Tab.this) MainActivity.this.updateNavigationButtons(); if (v.getTitle() != null && !v.getTitle().isEmpty()) { Tab.this.title = v.getTitle(); updateTabTitle(); } } @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { String u = r.getUrl().toString(); if (u.startsWith("http:") || u.startsWith("https:")) { Tab.this.initialUrl = u; return false; } if (u.startsWith("file://")) { Tab.this.initialUrl = u; loadLocalFile(u); return true; } try { Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(u)); startActivity(intent); return true; } catch (Exception e) { Log.w(TAG, "Could not handle non-http/https/file URL: " + u); return true; } } }); webView.setWebChromeClient(new WebChromeClient() { @Override public void onReceivedTitle(WebView v, String t) { if (t != null && !t.isEmpty()) { Tab.this.title = t; updateTabTitle(); } } @Override public void onGeolocationPermissionsShowPrompt(String o, GeolocationPermissions.Callback c) { new AlertDialog.Builder(MainActivity.this).setTitle("Location Request").setMessage(o + " wants to know your location.").setPositiveButton("Allow", (d, w) -> c.invoke(o, true, false)).setNegativeButton("Deny", (d, w) -> c.invoke(o, false, false)).show(); } @Override public boolean onConsoleMessage(ConsoleMessage m) { Log.d(TAG, "Console: " + m.message() + " -- From line " + m.lineNumber() + " of " + m.sourceId()); return super.onConsoleMessage(m); } @Override public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> cb, FileChooserParams p) { Intent intent = p.createIntent(); try { MainActivity.this.startActivityForResult(intent, FILE_PICKER_REQUEST_CODE); } catch (android.content.ActivityNotFoundException e) { Toast.makeText(MainActivity.this, "Cannot open file chooser", Toast.LENGTH_LONG).show(); return false; } return true; } }); webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, size) -> { String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype); Toast.makeText(MainActivity.this, "Downloading: " + fileName, Toast.LENGTH_LONG).show(); downloadHistory.add(0, url + " (" + fileName + ")"); DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)); request.setMimeType(mimetype); String cookies = CookieManager.getInstance().getCookie(url); if (cookies != null) request.addRequestHeader("cookie", cookies); request.addRequestHeader("User-Agent", userAgent); request.setDescription("Downloading file...").setTitle(fileName); request.allowScanningByMediaScanner(); request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); try { request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName); DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE); if (dm != null) dm.enqueue(request); else showError("Download Manager service not available."); } catch (Exception e) { showError("Download failed: " + e.getMessage() + "\nCheck storage permissions?"); } }); }
        private void setupTabView() { tabView = new LinearLayout(MainActivity.this); tabView.setOrientation(LinearLayout.HORIZONTAL); tabView.setPadding(8, 4, 8, 4); tabView.setGravity(Gravity.CENTER_VERTICAL); titleView = new TextView(MainActivity.this); titleView.setTextColor(Color.WHITE); titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12); titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START); titleView.setEllipsize(TextUtils.TruncateAt.END); titleView.setMaxLines(1); titleView.setOnClickListener(v -> MainActivity.this.switchToTab(this)); closeButton = new ImageButton(MainActivity.this); closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); closeButton.setBackgroundResource(0); closeButton.setColorFilter(0x99FFFFFF, android.graphics.PorterDuff.Mode.SRC_IN); closeButton.setScaleX(0.6f); closeButton.setScaleY(0.6f); closeButton.setPadding(4, 4, 4, 4); closeButton.setOnClickListener(v -> MainActivity.this.closeTab(this)); LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); titleParams.setMarginEnd(4); tabView.addView(titleView, titleParams); tabView.addView(closeButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)); GradientDrawable shape = new GradientDrawable(); shape.setShape(GradientDrawable.RECTANGLE); shape.setCornerRadius(6); shape.setColor(0xFF333333); shape.setStroke(1, 0x55666666); tabView.setBackground(shape); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()), ViewGroup.LayoutParams.MATCH_PARENT); params.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()); params.setMargins(4, 2, 4, 2); tabView.setLayoutParams(params); tabView.setMinimumWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())); updateTabTitle(); }
        void updateTabTitle() { titleView.setText((title != null && !title.isEmpty()) ? title : "Loading..."); }
        String getInitialUrl() { return (initialUrl != null && !initialUrl.isEmpty()) ? initialUrl : url; }
    }

    // --- Standard Browser Methods ---
    private void switchToTab(Tab tab) { if (tab == null || tabs.isEmpty() || (currentTab == tab && webViewContainer.indexOfChild(currentTab.webView) != -1)) return; if (currentTab != null) { if (currentTab.webView != null) { currentTab.webView.onPause(); currentTab.webView.setVisibility(View.GONE); webViewContainer.removeView(currentTab.webView); } if (currentTab.tabView != null && currentTab.tabView.getBackground() instanceof GradientDrawable) { GradientDrawable bg = (GradientDrawable) currentTab.tabView.getBackground(); bg.setColor(0xFF333333); bg.setStroke(1, 0x55666666); } } currentTab = tab; if (currentTab.webView == null) currentTab.setupWebView(); if (currentTab.webView.getParent() != null) ((ViewGroup)currentTab.webView.getParent()).removeView(currentTab.webView); currentTab.webView.setVisibility(View.VISIBLE); webViewContainer.addView(tab.webView, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); if (currentTab.tabView != null && currentTab.tabView.getBackground() instanceof GradientDrawable) { GradientDrawable bg = (GradientDrawable) tab.tabView.getBackground(); bg.setColor(0xFF555555); bg.setStroke(1, 0xFF888888); } urlBar.setText(tab.url); urlBar.setSelectAllOnFocus(true); updateNavigationButtons(); currentTab.webView.onResume(); tab.tabView.post(() -> { int scrollX = tab.tabView.getLeft() - (tabScroll.getWidth() - tab.tabView.getWidth()) / 2; tabScroll.smoothScrollTo(Math.max(0, scrollX), 0); }); }
    private void closeTab(Tab tab) {
        if (tabs.size() <= 1) {
             // *** CHANGE 2 of 3: Load asset home page when trying to close last tab ***
            if(currentTab != null && currentTab.webView != null && !currentTab.getInitialUrl().equals(HOME_PAGE_URL))
                loadUrlOrSearch(HOME_PAGE_URL);
            return;
        }
        int closingTabIndex = tabs.indexOf(tab);
        Tab tabToSwitchTo = (currentTab == tab) ? ((closingTabIndex > 0) ? tabs.get(closingTabIndex - 1) : tabs.get(1)) : null;
        tabLayout.removeView(tab.tabView); tabs.remove(tab);
        if (tabToSwitchTo != null) { if(tab.webView != null && tab.webView.getParent() == webViewContainer) webViewContainer.removeView(tab.webView); switchToTab(tabToSwitchTo); }
        final WebView webViewToDestroy = tab.webView;
        if (webViewToDestroy != null) webViewToDestroy.postDelayed(() -> { ViewGroup parent = (ViewGroup) webViewToDestroy.getParent(); if (parent != null) parent.removeView(webViewToDestroy); webViewToDestroy.destroy(); }, 100);
    }
    public void createNewTab(String urlToLoad) {
        Tab tab = new Tab(); tabs.add(tab); tabLayout.addView(tab.tabView); switchToTab(tab);
        if (urlToLoad != null && !urlToLoad.trim().isEmpty()) {
            loadUrlOrSearch(urlToLoad);
        } else {
            // *** CHANGE 3 of 3: Load asset home page for new blank tabs ***
            loadUrlOrSearch(HOME_PAGE_URL);
        }
    }
    public void createNewTab() { createNewTab(null); }
    private void handleContentUri(Uri uri) { if (currentTab == null || currentTab.webView == null) { showError("Cannot open file: No active tab."); return; } String mimeType = getContentResolver().getType(uri); String urlToLoad; if ("file".equals(uri.getScheme())) { urlToLoad = uri.toString(); } else if ("content".equals(uri.getScheme())) { try (InputStream is = getContentResolver().openInputStream(uri)) { if (is != null && (mimeType != null && (mimeType.startsWith("text/") || mimeType.equals("application/xhtml+xml") || mimeType.equals("application/xml")))) { String content = readStreamToString(is); String baseUrl = "content://" + uri.getAuthority() + "/"; currentTab.webView.loadDataWithBaseURL(baseUrl, content, mimeType, "UTF-8", null); currentTab.url = uri.toString(); currentTab.initialUrl = uri.toString(); urlBar.setText(currentTab.url); updateNavigationButtons(); return; } else { urlToLoad = uri.toString(); } } catch (IOException | SecurityException e) { showError("Error loading content URI: " + e.getMessage()); return; } } else { showError("Unsupported URI scheme for loading: " + uri.getScheme()); return; } currentTab.webView.loadUrl(urlToLoad); currentTab.url = urlToLoad; currentTab.initialUrl = urlToLoad; urlBar.setText(currentTab.url); updateNavigationButtons(); }
    private void loadLocalFile(String path) { if (currentTab == null || currentTab.webView == null) { showError("Cannot open file: No active tab."); return; } try { Uri uri = Uri.parse(path); File file; if ("file".equals(uri.getScheme()) && uri.getPath() != null) file = new File(uri.getPath()); else if (path.startsWith("/")) { file = new File(path); path = Uri.fromFile(file).toString(); } else if (path.startsWith("file:///android_asset/")) { /* Allow asset path directly */ } else { showError("Invalid file path format: " + path); return; } /* Removed file.exists() and file.canRead() checks as they don't work for assets */ currentTab.webView.loadUrl(path); currentTab.url = path; currentTab.initialUrl = path; urlBar.setText(currentTab.url); updateNavigationButtons(); } catch (Exception e) { showError("Error loading file: " + e.getMessage()); } }
    private String readStreamToString(InputStream is) throws IOException { ByteArrayOutputStream baos = new ByteArrayOutputStream(); byte[] buffer = new byte[4096]; int length; while ((length = is.read(buffer)) != -1) baos.write(buffer, 0, length); return baos.toString("UTF-8"); }
    private void showError(String message) { runOnUiThread(() -> { Log.e(TAG, "Error: " + message); if (!isFinishing()) new AlertDialog.Builder(this).setTitle("Error").setMessage(message).setPositiveButton("OK", null).show(); }); }


    // --- Activity Lifecycle & Setup ---
     @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        uiHandler = new Handler(Looper.getMainLooper());
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        aiInteractionLog = new ArrayList<>(); // Initialize log
        rootLayout = new FrameLayout(this);
        setupWebViewContainer();
        setupControls();
        setContentView(rootLayout);
        if (0 != (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        if (tabs.isEmpty()) { createNewTab(); } // Will load HOME_PAGE_URL due to change above
        else { currentTab = tabs.get(0); switchToTab(currentTab); }
        handleIntent(getIntent());
        updateActionButtonState();
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); handleIntent(intent); }
    private void handleIntent(Intent intent) {
        if (currentTab == null) { if (tabs.isEmpty()) createNewTab(); if (!tabs.isEmpty()) switchToTab(tabs.get(0)); if (currentTab == null) { showError("Cannot handle request: No active tab available."); return; }}
        if (intent == null || intent.getData() == null) {
            // Check if the current tab is empty or already the home page, if so, load it (again).
            // Note: `about:` is no longer used for the home page.
            if (currentTab.getInitialUrl() == null || currentTab.getInitialUrl().isEmpty() || currentTab.getInitialUrl().equals(HOME_PAGE_URL))
                loadUrlOrSearch(HOME_PAGE_URL); // Load asset home page if no specific intent data
            return;
        }
        Uri uri = intent.getData(); String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) loadLocalFile(uri.toString());
        else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) handleContentUri(uri);
        else if ("http".equals(scheme) || "https".equals(scheme)) loadUrlOrSearch(uri.toString());
        else showError("Cannot open content with scheme: " + scheme);
    }


    // --- UI Setup Methods ---
    private void updateWebViewContainerPadding() { if (webViewContainer == null || controls == null) return; FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webViewContainer.getLayoutParams(); params.topMargin = isFullscreen ? 0 : (controls.getHeight() > 0 ? controls.getHeight() : (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics())); webViewContainer.setLayoutParams(params); }
    private void setupWebViewContainer() { webViewContainer = new FrameLayout(this); FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); params.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics()); webViewContainer.setLayoutParams(params); GradientDrawable spaceGradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] {0xFF000814, 0xFF000C2B, 0xFF001440, 0xFF000814}); webViewContainer.setBackground(spaceGradient); rootLayout.addView(webViewContainer); }
    private void setupControls() { controls = new LinearLayout(this); controls.setOrientation(LinearLayout.HORIZONTAL); controls.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP)); GradientDrawable controlsBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFF000C2B, 0xFF000814}); controls.setBackground(controlsBg); controls.setPadding(4, 4, 4, 4); controls.setMinimumHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())); LinearLayout navSection = new LinearLayout(this); navSection.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)); setupNavigationButtons(navSection); controls.addView(navSection); LinearLayout urlSection = new LinearLayout(this); urlSection.setOrientation(LinearLayout.HORIZONTAL); urlSection.setGravity(Gravity.CENTER_VERTICAL); LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f); urlParams.setMargins(8, 0, 8, 0); urlSection.setLayoutParams(urlParams); urlSection.setPadding(8, 4, 8, 4); setupUrlBar(urlSection); setupSearchButton(urlSection); controls.addView(urlSection); LinearLayout tabSection = new LinearLayout(this); tabSection.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams tabSectionParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f); tabSection.setLayoutParams(tabSectionParams); tabSection.setGravity(Gravity.CENTER_VERTICAL | Gravity.START); setupTabSection(tabSection); controls.addView(tabSection); LinearLayout actionSection = new LinearLayout(this); actionSection.setOrientation(LinearLayout.HORIZONTAL); actionSection.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)); actionSection.setGravity(Gravity.CENTER_VERTICAL); setupActionButtons(actionSection); controls.addView(actionSection); rootLayout.addView(controls); accentLine = new View(this); GradientDrawable accentGlow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {0x000066CC, 0x400066CC, 0x000066CC}); accentLine.setBackground(accentGlow); controls.post(() -> { controlsHeight = controls.getHeight(); if (accentLine.getParent() == null) { FrameLayout.LayoutParams accentParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2); accentParams.topMargin = controlsHeight; accentLine.setLayoutParams(accentParams); rootLayout.addView(accentLine); } else { FrameLayout.LayoutParams currentParams = (FrameLayout.LayoutParams) accentLine.getLayoutParams(); currentParams.topMargin = controlsHeight; accentLine.setLayoutParams(currentParams); } updateWebViewContainerPadding(); }); setupTouchZone(); }
    private ImageButton createImageButton(int iconResId, View.OnClickListener listener, int colorFilter) { ImageButton button = new ImageButton(this); button.setImageResource(iconResId); button.setBackgroundResource(0); button.setColorFilter(colorFilter); button.setPadding(8, 0, 8, 0); button.setOnClickListener(listener); button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)); return button; }
    private void setupNavigationButtons(LinearLayout container) { int buttonColor = 0xFF0066CC; container.addView(createImageButton(android.R.drawable.ic_media_previous, v -> { if (currentTab != null && currentTab.webView != null && currentTab.webView.canGoBack()) currentTab.webView.goBack(); }, buttonColor)); container.addView(createImageButton(android.R.drawable.ic_media_next, v -> { if (currentTab != null && currentTab.webView != null && currentTab.webView.canGoForward()) currentTab.webView.goForward(); }, buttonColor)); container.addView(createImageButton(android.R.drawable.ic_menu_rotate, v -> { if (currentTab != null && currentTab.webView != null) currentTab.webView.reload(); }, buttonColor)); container.getChildAt(0).setId(android.R.id.button1); container.getChildAt(1).setId(android.R.id.button2); container.getChildAt(2).setId(android.R.id.button3); updateNavigationButtons(); }
    private void updateNavigationButtons() { runOnUiThread(() -> { boolean canGoBack = false, canGoForward = false, canReload = (currentTab != null && currentTab.webView != null); if (canReload) { canGoBack = currentTab.webView.canGoBack(); canGoForward = currentTab.webView.canGoForward(); } if (controls != null && controls.getChildCount() > 0 && controls.getChildAt(0) instanceof LinearLayout) { LinearLayout navSection = (LinearLayout) controls.getChildAt(0); ImageButton back = navSection.findViewById(android.R.id.button1), forward = navSection.findViewById(android.R.id.button2), reload = navSection.findViewById(android.R.id.button3); if (back != null) { back.setEnabled(canGoBack); back.setAlpha(canGoBack ? 1.0f : 0.5f); } if (forward != null) { forward.setEnabled(canGoForward); forward.setAlpha(canGoForward ? 1.0f : 0.5f); } if (reload != null) { reload.setEnabled(canReload); reload.setAlpha(canReload ? 1.0f : 0.5f); } } }); }
    private void setupUrlBar(LinearLayout container) { GradientDrawable urlBg = new GradientDrawable(); urlBg.setColor(0xFF001133); urlBg.setStroke(1, 0xFF0066CC); urlBg.setCornerRadius(2); urlBar = new EditText(this); urlBar.setBackground(urlBg); urlBar.setHint("URL or Search"); urlBar.setTextColor(0xFFCCE6FF); urlBar.setHintTextColor(0x80CCE6FF); urlBar.setSingleLine(true); urlBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); urlBar.setPadding(16, 8, 16, 8); urlBar.setImeOptions(EditorInfo.IME_ACTION_GO); urlBar.setSelectAllOnFocus(true); urlBar.setOnEditorActionListener((v, actionId, event) -> { if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) { loadUrlOrSearch(urlBar.getText().toString()); InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); if (imm != null) imm.hideSoftInputFromWindow(urlBar.getWindowToken(), 0); urlBar.clearFocus(); return true; } return false; }); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); params.gravity = Gravity.CENTER_VERTICAL; container.addView(urlBar, params); }
    private void setupSearchButton(LinearLayout container) { int buttonColor = 0xFF0066CC; container.addView(createImageButton(android.R.drawable.ic_menu_search, v -> loadUrlOrSearch(urlBar.getText().toString()), buttonColor)); }
    private void setupTabSection(LinearLayout container) { tabScroll = new HorizontalScrollView(this); tabScroll.setHorizontalScrollBarEnabled(false); tabLayout = new LinearLayout(this); tabLayout.setOrientation(LinearLayout.HORIZONTAL); tabLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START); tabScroll.addView(tabLayout); LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1); scrollParams.gravity = Gravity.CENTER_VERTICAL; container.addView(tabScroll, scrollParams); int buttonColor = 0xFF0066CC; container.addView(createImageButton(android.R.drawable.ic_menu_add, v -> createNewTab(), buttonColor)); }
    private void setupActionButtons(LinearLayout container) { int buttonColor = 0xFF0066CC; container.addView(createImageButton(android.R.drawable.ic_dialog_dialer, v -> toggleGridOverlay(), buttonColor)); aiButtonControl = createImageButton(android.R.drawable.ic_menu_send, v -> captureWebViewAndPromptAI(), buttonColor); container.addView(aiButtonControl); container.addView(createImageButton(android.R.drawable.ic_menu_recent_history, v -> showAiLogDialog(), buttonColor)); container.addView(createImageButton(android.R.drawable.ic_menu_view, v -> toggleFullscreen(), buttonColor)); container.addView(createImageButton(android.R.drawable.ic_menu_more, this::showMenu, buttonColor)); }
    private void setupTouchZone() { touchZone = new View(this); touchZone.setBackgroundColor(Color.TRANSPARENT); touchZone.setVisibility(View.GONE); touchZone.setOnTouchListener((v, event) -> { if (event.getAction() == MotionEvent.ACTION_DOWN) { int sensitiveHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()); if (event.getY() < sensitiveHeight) { toggleFullscreen(); return true; } } return false; }); rootLayout.addView(touchZone, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); }
    private void toggleFullscreen() { if (isAiSequenceRunning) return; isFullscreen = !isFullscreen; if (isFullscreen) { controls.setVisibility(View.GONE); if (accentLine != null) accentLine.setVisibility(View.GONE); if (touchZone != null) touchZone.setVisibility(View.VISIBLE); hideSystemUI(); } else { controls.setVisibility(View.VISIBLE); if (accentLine != null) accentLine.setVisibility(View.VISIBLE); if (touchZone != null) touchZone.setVisibility(View.GONE); showSystemUI(); } updateWebViewContainerPadding(); }
    private void hideSystemUI() { View decorView = getWindow().getDecorView(); decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN); }
    private void showSystemUI() { View decorView = getWindow().getDecorView(); decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN); }


    // --- API Key Management Methods ---
     private String loadSelectedApiKey() {
        String keysString = sharedPreferences.getString(API_KEYS_PREF, "");
        int selectedIndex = sharedPreferences.getInt(SELECTED_API_KEY_INDEX_PREF, 0);
        if (keysString.isEmpty() || selectedIndex <= 0) { Log.w(TAG, "API Keys not configured or index not set."); return null; }
        List<String> parts = Arrays.asList(keysString.split(",")); String selectedKey = null;
        try {
            for (int i = 0; i < parts.size(); i++) {
                try {
                    int currentNumber = Integer.parseInt(parts.get(i).trim());
                    if (currentNumber == selectedIndex && i + 1 < parts.size()) { selectedKey = parts.get(i + 1).trim(); break; }
                } catch (NumberFormatException e) { /* Ignore non-numeric parts */ }
            }
        } catch (Exception e) { Log.e(TAG, "Error parsing stored API keys string: " + keysString, e); return null; }
        if (selectedKey != null && !selectedKey.isEmpty()) { Log.d(TAG, "Using API Key at index " + selectedIndex); return selectedKey; }
        else { Log.w(TAG, "Selected API key index " + selectedIndex + " not found or key is empty in string: " + keysString); return null; }
    }
    private void saveApiKeys(String keysString) {
        if (keysString == null) return; keysString = keysString.trim();
        int selectedIndex = 0; String keyToStore = "";
        List<String> parts = Arrays.asList(keysString.split(",")); List<String> validPartsToStore = new ArrayList<>();
        try {
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i).trim(); if (part.isEmpty()) continue;
                try {
                    int currentNumber = Integer.parseInt(part);
                    if (i + 1 < parts.size() && !parts.get(i + 1).trim().isEmpty()) {
                        validPartsToStore.add(part); validPartsToStore.add(parts.get(i + 1).trim()); selectedIndex = currentNumber; i++;
                    } else { Log.w(TAG,"Index " + currentNumber + " found but no valid key follows."); }
                } catch (NumberFormatException e) { Log.w(TAG,"Ignoring part '" + part + "' as it's not a valid index or key following an index."); }
            }
             keyToStore = TextUtils.join(",", validPartsToStore);
        } catch (Exception e) { showError("Error parsing API key input. Please use format like '1,keyA,2,keyB,...'"); Log.e(TAG, "Error parsing user input API keys string: " + keysString, e); return; }
        SharedPreferences.Editor editor = sharedPreferences.edit(); editor.putString(API_KEYS_PREF, keyToStore); editor.putInt(SELECTED_API_KEY_INDEX_PREF, selectedIndex); editor.apply();
        if (selectedIndex > 0 && !keyToStore.isEmpty()) { Toast.makeText(this, "API Keys saved. Selected index: " + selectedIndex, Toast.LENGTH_SHORT).show(); }
        else if (!keyToStore.isEmpty()){ Toast.makeText(this, "API Keys saved, but no valid index found/set.", Toast.LENGTH_LONG).show(); }
        else { Toast.makeText(this, "API Keys cleared.", Toast.LENGTH_SHORT).show(); }
    }
    private void showApiKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); builder.setTitle("API Key Settings");
        final EditText input = new EditText(this); input.setHint("Format: 1,key_one,2,key_two,..."); input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); input.setPadding(40, 40, 40, 40);
        String currentKeys = sharedPreferences.getString(API_KEYS_PREF, ""); input.setText(currentKeys); input.setSelection(input.getText().length());
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> { String inputText = input.getText().toString(); saveApiKeys(inputText); hideKeyboard(input); });
        builder.setNegativeButton("Cancel", (dialog, which) -> { dialog.cancel(); hideKeyboard(input); });
        AlertDialog dialog = builder.create(); dialog.setOnShowListener(di -> showKeyboard(input)); dialog.show();
    }


    // --- Grid/Target Logic ---
     private void toggleGridOverlay() { if (isAiSequenceRunning) { showError("AI sequence in progress."); return; } if (isGridVisible) removeGrid(); else promptGridDensity(); }
    private void promptGridDensity() { final EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER); input.setHint("Lines per side (e.g., 10)"); input.setText(String.valueOf(currentGridDensity)); input.setSelection(input.getText().length()); AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Grid Density").setMessage("Enter number of horizontal/vertical lines:").setView(input).setPositiveButton("Show Grid", (d, which) -> { String numStr = input.getText().toString(); int density = 0; try { density = Integer.parseInt(numStr); } catch (NumberFormatException e) { /* Handled below */ } if (density > 0 && density <= 50) showGrid(density); else showError("Please enter a valid positive number (1-50)."); hideKeyboard(input); }).setNegativeButton("Cancel", (d,w) -> hideKeyboard(input)).create(); dialog.setOnShowListener(di -> showKeyboard(input)); dialog.show(); }
    private void showGrid(int density) { if (webViewContainer == null) return; removeGrid(); currentGridDensity = density; gridOverlayView = new GridView(this); gridOverlayView.setGridDensity(density); webViewContainer.addView(gridOverlayView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); isGridVisible = true; }
    private void removeGrid() { if (gridOverlayView != null && webViewContainer != null) webViewContainer.removeView(gridOverlayView); gridOverlayView = null; isGridVisible = false; }
    private void showKeyboard(View view) { if (view.requestFocus()) { InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT); } }
    private void hideKeyboard(View view) { InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0); }
    private void promptForGridTarget() { if (isAiSequenceRunning) { showError("AI sequence in progress."); return; } if (!isGridVisible) { showError("Grid must be visible to manually select a target."); return; } final EditText input = new EditText(this); input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS); input.setHint("e.g., R5C8"); AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Target Grid Cell").setMessage("Enter the target cell label:").setView(input).setPositiveButton("Target", (d, which) -> { String label = input.getText().toString().trim().toUpperCase(); if (!label.isEmpty()) simulateClickOnGridCell(label); else showError("Please enter a cell label."); hideKeyboard(input); }).setNegativeButton("Cancel", (d,w) -> hideKeyboard(input)).create(); dialog.setOnShowListener(di -> showKeyboard(input)); dialog.show(); }
    private void simulateClickOnGridCell(String cellLabel) { GridView tempGridForCoords = new GridView(this); tempGridForCoords.setGridDensity(currentGridDensity); int width = webViewContainer != null ? webViewContainer.getWidth() : 0; int height = webViewContainer != null ? webViewContainer.getHeight() : 0; if (width <= 0 || height <= 0) { showError("Cannot simulate click: Invalid container dimensions."); endAiSequence(); return; } tempGridForCoords.layout(0, 0, width, height); if (currentTab == null || currentTab.webView == null) { showError("No active webview to click on."); endAiSequence(); return; } float[] coords = tempGridForCoords.getCenterCoordinatesForLabel(cellLabel); if (coords != null) { final float clickX = coords[0]; final float clickY = coords[1]; currentTab.webView.post(() -> { try { long downTime = SystemClock.uptimeMillis(); long eventTime = SystemClock.uptimeMillis(); int metaState = 0; MotionEvent motionEventDown = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, clickX, clickY, metaState); currentTab.webView.dispatchTouchEvent(motionEventDown); eventTime = SystemClock.uptimeMillis() + 50; MotionEvent motionEventUp = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, clickX, clickY, metaState); currentTab.webView.dispatchTouchEvent(motionEventUp); motionEventDown.recycle(); motionEventUp.recycle(); } catch (Exception e) { showError("Failed to dispatch touch event: " + e.getMessage()); endAiSequence(); } }); } else { showError("Invalid grid cell label: " + cellLabel); endAiSequence(); } }
    private List<KeyEvent> getKeyEventsForChar(char c, long downTime) { List<KeyEvent> events = new ArrayList<>(); int keyCode = -1; int metaState = 0; if (c >= 'a' && c <= 'z') keyCode = KeyEvent.KEYCODE_A + (c - 'a'); else if (c >= 'A' && c <= 'Z') { keyCode = KeyEvent.KEYCODE_A + (c - 'A'); metaState = KeyEvent.META_SHIFT_ON; } else if (c >= '0' && c <= '9') keyCode = KeyEvent.KEYCODE_0 + (c - '0'); else if (c == ' ') keyCode = KeyEvent.KEYCODE_SPACE; else if (c == '.') keyCode = KeyEvent.KEYCODE_PERIOD; else if (c == ',') keyCode = KeyEvent.KEYCODE_COMMA; else if (c == '/') keyCode = KeyEvent.KEYCODE_SLASH; else if (c == ':') { keyCode = KeyEvent.KEYCODE_SEMICOLON; metaState = KeyEvent.META_SHIFT_ON; } else if (c == '-') keyCode = KeyEvent.KEYCODE_MINUS; else if (c == '+') keyCode = KeyEvent.KEYCODE_PLUS; else if (c == '=') keyCode = KeyEvent.KEYCODE_EQUALS; else if (c == '(') { keyCode = KeyEvent.KEYCODE_9; metaState = KeyEvent.META_SHIFT_ON; } else if (c == ')') { keyCode = KeyEvent.KEYCODE_0; metaState = KeyEvent.META_SHIFT_ON; } else if (c == '[') keyCode = KeyEvent.KEYCODE_LEFT_BRACKET; else if (c == ']') keyCode = KeyEvent.KEYCODE_RIGHT_BRACKET; else if (c == '"') keyCode = KeyEvent.KEYCODE_APOSTROPHE; // Often requires shift depending on layout
      if (keyCode != -1) { long eventTime = SystemClock.uptimeMillis(); events.add(new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0, metaState)); eventTime = SystemClock.uptimeMillis() + 10; events.add(new KeyEvent(downTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0, metaState)); } else Log.w(TAG, "No key mapping for character: " + c); return events; }
    private void simulateTypeCommand(String cellLabel, String textToType) { GridView tempGridForCoords = new GridView(this); tempGridForCoords.setGridDensity(currentGridDensity); int width = webViewContainer != null ? webViewContainer.getWidth() : 0; int height = webViewContainer != null ? webViewContainer.getHeight() : 0; if (width <= 0 || height <= 0) { showError("Cannot simulate type: Invalid container dimensions."); endAiSequence(); return; } tempGridForCoords.layout(0, 0, width, height); if (currentTab == null || currentTab.webView == null) { showError("No active webview to type into."); endAiSequence(); return; } float[] coords = tempGridForCoords.getCenterCoordinatesForLabel(cellLabel); if (coords == null) { showError("Invalid grid cell label for typing: " + cellLabel); endAiSequence(); return; } final float clickX = coords[0]; final float clickY = coords[1]; final String finalText = textToType; currentTab.webView.post(() -> { try { long downTime = SystemClock.uptimeMillis(); long eventTime = SystemClock.uptimeMillis(); int metaState = 0; MotionEvent motionEventDown = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, clickX, clickY, metaState); currentTab.webView.dispatchTouchEvent(motionEventDown); eventTime = SystemClock.uptimeMillis() + 50; MotionEvent motionEventUp = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, clickX, clickY, metaState); currentTab.webView.dispatchTouchEvent(motionEventUp); motionEventDown.recycle(); motionEventUp.recycle(); } catch (Exception e) { showError("Failed to dispatch focus tap for typing: " + e.getMessage()); endAiSequence(); return; } currentTab.webView.postDelayed(() -> { long typeDownTime = SystemClock.uptimeMillis(); try { for (char c : finalText.toCharArray()) { List<KeyEvent> events = getKeyEventsForChar(c, typeDownTime); for (KeyEvent event : events) { currentTab.webView.dispatchKeyEvent(event); } SystemClock.sleep(50); } } catch (Exception e) { showError("Failed during key event dispatch: " + e.getMessage()); endAiSequence(); } }, 200); }); }
    private void simulateEnterKey(String cellLabel) { GridView tempGridForCoords = new GridView(this); tempGridForCoords.setGridDensity(currentGridDensity); int width = webViewContainer != null ? webViewContainer.getWidth() : 0; int height = webViewContainer != null ? webViewContainer.getHeight() : 0; if (width <= 0 || height <= 0) { showError("Cannot simulate enter: Invalid container dimensions."); endAiSequence(); return; } tempGridForCoords.layout(0, 0, width, height); if (currentTab == null || currentTab.webView == null) { showError("No active webview for Enter key."); endAiSequence(); return; } float[] coords = tempGridForCoords.getCenterCoordinatesForLabel(cellLabel); if (coords == null) { showError("Invalid grid cell label for Enter key: " + cellLabel); endAiSequence(); return; } final float clickX = coords[0]; final float clickY = coords[1]; currentTab.webView.post(() -> { try { long downTime = SystemClock.uptimeMillis(); long eventTime = SystemClock.uptimeMillis(); int metaState = 0; MotionEvent motionEventDown = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, clickX, clickY, metaState); currentTab.webView.dispatchTouchEvent(motionEventDown); eventTime = SystemClock.uptimeMillis() + 50; MotionEvent motionEventUp = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, clickX, clickY, metaState); currentTab.webView.dispatchTouchEvent(motionEventUp); motionEventDown.recycle(); motionEventUp.recycle(); } catch (Exception e) { showError("Failed to dispatch focus tap for Enter key: " + e.getMessage()); endAiSequence(); return; } currentTab.webView.postDelayed(() -> { long enterDownTime = SystemClock.uptimeMillis(); long enterEventTime = SystemClock.uptimeMillis(); try { KeyEvent downEvent = new KeyEvent(enterDownTime, enterEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0); currentTab.webView.dispatchKeyEvent(downEvent); enterEventTime = SystemClock.uptimeMillis() + 10; KeyEvent upEvent = new KeyEvent(enterDownTime, enterEventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0); currentTab.webView.dispatchKeyEvent(upEvent); } catch (Exception e) { showError("Failed during Enter key dispatch: " + e.getMessage()); endAiSequence(); } }, 200); }); }


    // --- Tab List Generation ---
     private String getTabListString() { StringBuilder tabListBuilder = new StringBuilder("Open Tabs:\n"); if (tabs == null || tabs.isEmpty()) { tabListBuilder.append("(No other tabs open)"); } else { for (int i = 0; i < tabs.size(); i++) { Tab t = tabs.get(i); String title = (t.title != null && !t.title.isEmpty()) ? t.title : "(No Title)"; String url = (t.url != null && !t.url.isEmpty()) ? t.url : "(No URL)"; tabListBuilder.append(String.format(Locale.US, "%d. %s (%s) %s\n", i + 1, title, url.length() > 50 ? url.substring(0,47)+"..." : url , (t == currentTab ? "(Current)" : ""))); } } return tabListBuilder.toString().trim(); }


    // --- AI Context Gathering ---
     interface AiContextCallback { void onContextReady(String base64Clean, String base64Grid); void onError(String errorMessage); }
    private void getAiContext(final AiContextCallback callback) { if (currentTab == null || currentTab.webView == null || webViewContainer == null) { callback.onError("Cannot get AI context: No active tab/webview/container."); return; } final WebView webView = currentTab.webView; final int width = webViewContainer.getWidth(); final int height = webViewContainer.getHeight(); if (width <= 0 || height <= 0) { callback.onError("Cannot get AI context: Invalid container dimensions."); return; } uiHandler.post(() -> { Bitmap bitmapClean = null; Bitmap bitmapGrid = null; String base64ImageClean = null; String base64ImageGrid = null; String captureError = null; try { bitmapClean = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); Canvas canvasClean = new Canvas(bitmapClean); webView.draw(canvasClean); base64ImageClean = encodeBitmapToBase64(bitmapClean); if (base64ImageClean == null) throw new IOException("Failed to encode clean bitmap."); bitmapGrid = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); Canvas canvasGrid = new Canvas(bitmapGrid); webView.draw(canvasGrid); GridView tempGridDrawer = new GridView(MainActivity.this); tempGridDrawer.setGridDensity(currentGridDensity); tempGridDrawer.layout(0, 0, width, height); tempGridDrawer.draw(canvasGrid); base64ImageGrid = encodeBitmapToBase64(bitmapGrid); if (base64ImageGrid == null) throw new IOException("Failed to encode grid bitmap."); } catch (OutOfMemoryError e) { captureError = "Screenshot too large (OutOfMemoryError)."; Log.e(TAG, captureError, e); } catch (Exception e) { captureError = "Failed during bitmap capture/encode: " + e.getMessage(); Log.e(TAG, captureError, e); } finally { if (bitmapClean != null && !bitmapClean.isRecycled()) bitmapClean.recycle(); if (bitmapGrid != null && !bitmapGrid.isRecycled()) bitmapGrid.recycle(); } if (captureError == null) { callback.onContextReady(base64ImageClean, base64ImageGrid); } else { callback.onError(captureError); } }); }
    private String encodeBitmapToBase64(Bitmap bitmap) { if (bitmap == null) return null; String base64Image = null; try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) { bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream); byte[] byteArray = byteArrayOutputStream.toByteArray(); base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP); } catch (IOException | OutOfMemoryError e) { Log.e(TAG, "Exception during Base64 encoding", e); return null; } return base64Image; }


    // --- AI Interaction Logic ---
     private void captureWebViewAndPromptAI() { if (isAiSequenceRunning) { showError("AI sequence already in progress."); return; } if (loadSelectedApiKey() == null) { showError("API Key not configured. Please set it via the menu."); showApiKeyDialog(); return; } isAiSequenceRunning = true; currentAiTurn = 1; aiInternalNote = ""; aiInteractionLog.clear(); updateActionButtonState(); Toast.makeText(this, "Getting AI context...", Toast.LENGTH_SHORT).show(); getAiContext(new AiContextCallback() { @Override public void onContextReady(String base64Clean, String base64Grid) { String tabListString = getTabListString(); promptForAiTextWithData(base64Clean, base64Grid, tabListString); } @Override public void onError(String errorMessage) { showError("Failed to get AI context: " + errorMessage); endAiSequence(); } }); }
    private void promptForAiTextWithData(String base64Clean, String base64Grid, String tabListString) { final EditText input = new EditText(this); input.setHint("Enter initial prompt for the AI..."); input.setLines(3); input.setGravity(Gravity.TOP | Gravity.START); AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Gemini Prompt (Turn " + currentAiTurn + ")").setMessage("Context captured. Enter your request:").setView(input) .setPositiveButton("Send to AI", (d, which) -> { String userPrompt = input.getText().toString().trim(); if (userPrompt.isEmpty()) { showError("Please enter a prompt for the AI."); endAiSequence(); return; } aiInteractionLog.add("User Request (Turn 1):\n" + userPrompt); String fullPrompt = SYSTEM_PROMPT + "\n--- Turn " + currentAiTurn + " of " + MAX_AI_TURNS + " ---\n" + "**User Request:** " + userPrompt; final String finalBase64Clean = base64Clean; final String finalBase64Grid = base64Grid; final String finalTabList = tabListString; Toast.makeText(MainActivity.this, "Calling AI (Turn " + currentAiTurn + ")...", Toast.LENGTH_SHORT).show(); new Thread(() -> callGeminiApi(fullPrompt, finalBase64Clean, finalBase64Grid, finalTabList, "")).start(); hideKeyboard(input); }) .setNegativeButton("Cancel", (d,w) -> { hideKeyboard(input); endAiSequence(); }) .setOnCancelListener(dialogInterface -> endAiSequence()) .create(); dialog.setOnShowListener(di -> showKeyboard(input)); dialog.show(); }
    private void captureAndRequestNextAiAction() { if (!isAiSequenceRunning) return; Toast.makeText(this, "Getting AI context for Turn " + currentAiTurn + "...", Toast.LENGTH_SHORT).show(); getAiContext(new AiContextCallback() { @Override public void onContextReady(String base64Clean, String base64Grid) { String tabListString = getTabListString(); String nextPrompt = SYSTEM_PROMPT + "\n--- Turn " + currentAiTurn + " of " + MAX_AI_TURNS + " ---\n" + "**Continue Task:** Choose the next action based on the context and the previous note/checklist."; final String finalBase64Clean = base64Clean; final String finalBase64Grid = base64Grid; final String finalTabList = tabListString; Toast.makeText(MainActivity.this, "Calling AI (Turn " + currentAiTurn + ")...", Toast.LENGTH_SHORT).show(); new Thread(() -> callGeminiApi(nextPrompt, finalBase64Clean, finalBase64Grid, finalTabList, aiInternalNote)).start(); } @Override public void onError(String errorMessage) { showError("Failed to get AI context for Turn " + currentAiTurn + ": " + errorMessage); endAiSequence(); } }); }
    private void callGeminiApi(String combinedPrompt, String base64ImageClean, String base64ImageGrid, String tabListString, String previousNote) { final String apiKey = loadSelectedApiKey(); if (apiKey == null) { runOnUiThread(() -> { showError("No valid API Key selected. Configure in Menu > API Key Settings."); }); endAiSequence(); return; } final String apiUrl = GEMINI_API_BASE_URL + apiKey; String jsonPayload = ""; try { JSONObject root = new JSONObject(); JSONArray contents = new JSONArray(); JSONObject content = new JSONObject(); JSONArray parts = new JSONArray(); JSONObject textPart = new JSONObject(); textPart.put("text", combinedPrompt); parts.put(textPart); if (previousNote != null && !previousNote.isEmpty()) { JSONObject notePart = new JSONObject(); notePart.put("text", "\n--- Previous AI Note/Checklist ---\n" + previousNote); parts.put(notePart); } if (base64ImageClean != null && !base64ImageClean.isEmpty()){ JSONObject imagePartClean = new JSONObject(); JSONObject inlineDataClean = new JSONObject(); inlineDataClean.put("mimeType", "image/png"); inlineDataClean.put("data", base64ImageClean); imagePartClean.put("inlineData", inlineDataClean); parts.put(imagePartClean); } else { Log.w(TAG, "Clean image data is null or empty, skipping."); } if (base64ImageGrid != null && !base64ImageGrid.isEmpty()){ JSONObject imagePartGrid = new JSONObject(); JSONObject inlineDataGrid = new JSONObject(); inlineDataGrid.put("mimeType", "image/png"); inlineDataGrid.put("data", base64ImageGrid); imagePartGrid.put("inlineData", inlineDataGrid); parts.put(imagePartGrid); } else { Log.w(TAG, "Grid image data is null or empty, skipping."); } JSONObject tabListPart = new JSONObject(); tabListPart.put("text", "\n" + tabListString); parts.put(tabListPart); content.put("parts", parts); contents.put(content); root.put("contents", contents); jsonPayload = root.toString(); } catch (JSONException e) { final String errorMsg = "Error creating JSON payload: " + e.getMessage(); runOnUiThread(() -> { showError(errorMsg); endAiSequence(); }); return; } HttpURLConnection connection = null; String responseText = ""; String errorText = null; try { URL url = new URL(apiUrl); connection = (HttpURLConnection) url.openConnection(); connection.setRequestMethod("POST"); connection.setRequestProperty("Content-Type", "application/json; utf-8"); connection.setRequestProperty("Accept", "application/json"); connection.setDoOutput(true); connection.setConnectTimeout(30000); connection.setReadTimeout(60000); try (OutputStream os = connection.getOutputStream()) { byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8); os.write(input, 0, input.length); } int responseCode = connection.getResponseCode(); InputStream stream = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream(); if (stream != null) { BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)); StringBuilder responseBuilder = new StringBuilder(); String line; while ((line = reader.readLine()) != null) responseBuilder.append(line.trim()); reader.close(); responseText = responseBuilder.toString(); } else responseText = "No response body received."; if (!(responseCode >= 200 && responseCode < 300)) errorText = "API Error (" + responseCode + "): " + responseText; } catch (SocketTimeoutException e) { errorText = "Network Timeout: " + e.getMessage(); } catch (IOException e) { errorText = "Network Error ("+apiUrl.substring(0, Math.min(apiUrl.length(), apiUrl.indexOf('?')+5))+"...): " + e.getMessage(); } catch (Exception e) { errorText = "Unexpected Error during API call: " + e.getMessage(); } finally { if (connection != null) connection.disconnect(); } final String finalResponse = responseText; final String finalError = errorText; runOnUiThread(() -> { if (finalError != null) { String message = "API Call Failed (Turn " + currentAiTurn + "):\n" + finalError; aiInteractionLog.add(message); showGeminiResponse(message); endAiSequence(); } else processAiTurnResponse(finalResponse); }); }

    /**
     * Processes the AI's response, handling commands and notes.
     * Ensures NOTE command format is validated robustly.
     * @param rawResponse The raw string response from the AI API.
     */
    private void processAiTurnResponse(String rawResponse) {
        if (!isAiSequenceRunning) return;
        String extractedResponse = "";
        aiInternalNote = ""; // Clear note initially for this turn's processing

        // --- Extract Text Response ---
        try {
            JSONObject responseObj = new JSONObject(rawResponse); JSONArray candidates = responseObj.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) { JSONObject candidate = candidates.getJSONObject(0); JSONObject content = candidate.optJSONObject("content"); if (content != null) { JSONArray parts = content.optJSONArray("parts"); if (parts != null && parts.length() > 0) { JSONObject firstPart = parts.getJSONObject(0); extractedResponse = firstPart.optString("text", "").trim(); } else extractedResponse = "(AI returned no text parts)"; } else extractedResponse = "(AI returned no content)"; }
            else { String blockReason = responseObj.optJSONObject("promptFeedback") != null ? responseObj.optJSONObject("promptFeedback").optString("blockReason", "Unknown") : "Unknown (No Prompt Feedback)"; if (!blockReason.equals("Unknown")){ extractedResponse = CMD_RESPOND + "AI blocked response. Reason: " + blockReason; Log.w(TAG, "AI Response Blocked: " + blockReason); } else { extractedResponse = "(Could not extract text from standard structure)"; Log.w(TAG, "Could not extract text from standard candidate structure: " + rawResponse); } }
        } catch (JSONException e) { extractedResponse = rawResponse.trim(); Log.w(TAG, "AI response not standard JSON or parsing error, using raw: " + extractedResponse, e); }

        aiInteractionLog.add("AI Response (Turn " + currentAiTurn + "):\n" + extractedResponse); // Log FULL response

        // --- Processing Logic ---
        String[] lines = extractedResponse.split("\\r?\\n", -1); // Split into lines
        boolean continueSequence = false; boolean actionError = false; boolean noteFoundThisTurn = false;

        // --- Trim and Process First Line (Primary Action) ---
        String firstLineRaw = lines.length > 0 ? lines[0].trim() : "";
        String firstLineCommand = trimCommandWrappers(firstLineRaw); // Trim quotes/backticks
        Log.i(TAG, "Processing AI First Line Command (Trimmed) (Turn " + currentAiTurn + "): " + firstLineCommand);

        if (firstLineCommand.startsWith(CMD_CLICK)) { String label = firstLineCommand.substring(CMD_CLICK.length()).trim(); if (!label.isEmpty() && label.matches("R\\d+C\\d+")) { Toast.makeText(this, "AI: CLICK " + label, Toast.LENGTH_SHORT).show(); simulateClickOnGridCell(label); } else { showError("AI Turn " + currentAiTurn + ": Invalid CLICK format: " + firstLineCommand); actionError = true; } }
        else if (firstLineCommand.startsWith(CMD_TYPE)) { String args = firstLineCommand.substring(CMD_TYPE.length()); int sep = args.indexOf(CMD_SEPARATOR); if (sep > 0) { String label = args.substring(0, sep).trim(); String text = args.substring(sep + CMD_SEPARATOR.length()).trim(); if (!label.isEmpty() && label.matches("R\\d+C\\d+")) { Toast.makeText(this, "AI: TYPE into " + label, Toast.LENGTH_SHORT).show(); simulateTypeCommand(label, text); } else { showError("AI Turn " + currentAiTurn + ": Invalid TYPE format (label): " + firstLineCommand); actionError = true; } } else { showError("AI Turn " + currentAiTurn + ": Invalid TYPE format (separator): " + firstLineCommand); actionError = true; } }
        else if (firstLineCommand.startsWith(CMD_ENTER)) { String label = firstLineCommand.substring(CMD_ENTER.length()).trim(); if (!label.isEmpty() && label.matches("R\\d+C\\d+")) { Toast.makeText(this, "AI: ENTER on " + label, Toast.LENGTH_SHORT).show(); simulateEnterKey(label); } else { showError("AI Turn " + currentAiTurn + ": Invalid ENTER format: " + firstLineCommand); actionError = true; } }
        else if (firstLineCommand.startsWith(CMD_NAVIGATE)) { String url = firstLineCommand.substring(CMD_NAVIGATE.length()).trim(); if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https:") || url.toLowerCase().startsWith("file://")) { Toast.makeText(this, "AI: NAVIGATE to " + url, Toast.LENGTH_SHORT).show(); loadUrlOrSearch(url); } else { showError("AI Turn " + currentAiTurn + ": Invalid NAVIGATE URL: " + firstLineCommand); actionError = true; } }
        else if (firstLineCommand.startsWith(CMD_NEW_TAB)) { String urlPart = firstLineCommand.substring(CMD_NEW_TAB.length()).trim(); String urlToLoad = null; if (urlPart.toLowerCase().startsWith("http://") || urlPart.toLowerCase().startsWith("https:") || urlPart.toLowerCase().startsWith("file://")) { urlToLoad = urlPart; Toast.makeText(this, "AI: NEW_TAB with URL", Toast.LENGTH_SHORT).show(); } else { Toast.makeText(this, "AI: NEW_TAB (blank)", Toast.LENGTH_SHORT).show(); if (!urlPart.isEmpty()) { Log.w(TAG, "NEW_TAB command had non-URL argument ignored: " + urlPart); } } createNewTab(urlToLoad); }
        else if (firstLineCommand.startsWith(CMD_NOTE)) { Toast.makeText(this, "AI: Processing Note...", Toast.LENGTH_SHORT).show(); } // Handled below
        else if (firstLineCommand.startsWith(CMD_RESPOND)) { String message = firstLineCommand.substring(CMD_RESPOND.length()).trim(); if (lines.length > 1) { StringBuilder fullMessage = new StringBuilder(message); for (int i = 1; i < lines.length; i++) { fullMessage.append("\n").append(lines[i]); } message = fullMessage.toString(); } showGeminiResponse("AI Final Response:\n" + message); continueSequence = false; } // Ends sequence
        else if (firstLineCommand.isEmpty() && lines.length <= 1) { showError("AI Turn " + currentAiTurn + ": Received empty command."); actionError = true; }
        else { boolean looksLikeNote = extractedResponse.contains("- [ ]") || extractedResponse.contains("- [x]"); if (currentAiTurn == 1 && looksLikeNote) { Log.w(TAG, "AI Turn 1: Response didn't start with NOTE :: but looks like checklist. Processing as NOTE."); Toast.makeText(this, "AI: Processing Note (Implicit)...", Toast.LENGTH_SHORT).show(); } else { showError("AI Turn " + currentAiTurn + ": Command format not recognized on first line: " + firstLineCommand); showGeminiResponse("Unrecognized AI Response (Turn " + currentAiTurn + "):\n" + extractedResponse); actionError = true; } }

        // --- Process NOTE (Expected on ALL turns except RESPOND) ---
        int noteLineIndex = -1; String trimmedNoteLine = "";
        for(int i = 0; i < lines.length; i++) {
            trimmedNoteLine = trimCommandWrappers(lines[i].trim()); // Trim wrappers
            if (trimmedNoteLine.startsWith(CMD_NOTE)) {
                noteLineIndex = i;
                break; // Found the note line
            }
        }
        // Handle implicit note on turn 1 if first line wasn't NOTE but looked like one
        if (noteLineIndex == -1 && currentAiTurn == 1 && !firstLineCommand.startsWith(CMD_NOTE) && (extractedResponse.contains("- [ ]") || extractedResponse.contains("- [x]"))) {
           noteLineIndex = 0;
           trimmedNoteLine = trimCommandWrappers(lines[0].trim()); // Re-trim the first line
        }

        // *** SIMPLIFIED AND ROBUST NOTE PARSING BLOCK ***
        if (noteLineIndex != -1 && !actionError) { // Don't process note if action already failed
            String noteHeader = trimmedNoteLine; // Use the trimmed line found/re-trimmed
            String noteContent = "";
            boolean noteFormatValid = false;

            if (noteHeader.startsWith(CMD_NOTE)) {
                // It starts with "NOTE ". Look for "::" anywhere *at or after* the position right after the prefix.
                int prefixEndIndex = CMD_NOTE.length(); // e.g., 5 for "NOTE "
                int sepIndex = noteHeader.indexOf("::", prefixEndIndex); // Search for "::" starting from index 5

                if (sepIndex != -1) {
                    // Found "::" at or after the end of "NOTE ".
                    int contentStartIndex = sepIndex + 2; // Index right after "::"
                    // Extract the substring and trim spaces *from the content part only*
                    if (contentStartIndex <= noteHeader.length()) {
                        noteContent = noteHeader.substring(contentStartIndex).trim();
                    } else {
                        noteContent = ""; // Separator was at the very end
                    }
                    noteFormatValid = true;
                } else {
                    // Started with "NOTE " but "::" was missing after it.
                    showError("AI Turn " + currentAiTurn + ": Invalid NOTE format (missing '::' separator after 'NOTE '): " + noteHeader);
                    actionError = true;
                }
            } else if (noteLineIndex == 0 && currentAiTurn == 1) {
                 // Implicit Turn 1 note (doesn't start with NOTE but looks like checklist)
                 noteContent = noteHeader; // Use the whole (trimmed) first line as content
                 noteFormatValid = true; // Assume valid if it looks like a checklist implicitly
            }
            // No explicit 'else' needed: if it didn't start with NOTE and wasn't implicit, it's not a note line we parse here.

            // If the format was determined valid (explicit or implicit), proceed
            if (noteFormatValid && !actionError) {
                // Append subsequent lines (raw) to the note content
                StringBuilder fullNote = new StringBuilder(noteContent); // Start with the parsed content from the header line
                for (int i = noteLineIndex + 1; i < lines.length; i++) {
                    fullNote.append("\n").append(lines[i]); // Append subsequent raw lines
                }
                aiInternalNote = fullNote.toString().trim(); // Store the full note content
                noteFoundThisTurn = true;
                Log.d(TAG, "AI Note stored for next turn:\n" + aiInternalNote);
                // If NOTE was found, we continue the sequence (unless RESPOND was also found)
                if (!firstLineCommand.startsWith(CMD_RESPOND)) {
                    continueSequence = true;
                }
            }
        }
        // *** END OF SIMPLIFIED NOTE PARSING BLOCK ***


        // --- Final Decision: Continue or End? ---
        // Note is REQUIRED on EVERY turn *unless* the command is RESPOND.
        boolean noteRequired = !firstLineCommand.startsWith(CMD_RESPOND);

        if (noteRequired && !noteFoundThisTurn && !actionError) {
            // If NOTE wasn't found, but the first line wasn't RESPOND and there wasn't already an error
            showError("AI Turn " + currentAiTurn + ": Missing required NOTE command.");
            actionError = true; // Treat missing note as an error
            continueSequence = false;
        }

        if (continueSequence && !actionError) {
            scheduleNextAiTurn();
        } else {
            endAiSequence(); // End on error, RESPOND, or missing required note
        }
    }
    private String trimCommandWrappers(String command) { if (command == null) return ""; String trimmed = command.trim(); if (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length() >= 2) { trimmed = trimmed.substring(1, trimmed.length() - 1); } if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) { trimmed = trimmed.substring(1, trimmed.length() - 1); } if (trimmed.startsWith("`") && trimmed.endsWith("`") && trimmed.length() >= 2) { trimmed = trimmed.substring(1, trimmed.length() - 1); } return trimmed.trim(); }
    private void scheduleNextAiTurn() { if (!isAiSequenceRunning) return; currentAiTurn++; if (currentAiTurn > MAX_AI_TURNS) { String msg = "Max AI turns (" + MAX_AI_TURNS + ") reached."; showError(msg); aiInteractionLog.add("System: " + msg); endAiSequence(); return; } Toast.makeText(this, "Waiting 5s for next AI turn ("+currentAiTurn+")...", Toast.LENGTH_SHORT).show(); uiHandler.postDelayed(this::captureAndRequestNextAiAction, 5000); }
    private void endAiSequence() { if (!isAiSequenceRunning) return; Log.i(TAG, "Ending AI Sequence."); if (currentAiTurn > 0) { aiInteractionLog.add("System: AI Sequence Ended (Turn " + currentAiTurn + ")"); } isAiSequenceRunning = false; currentAiTurn = 0; aiInternalNote = ""; removeGrid(); updateActionButtonState(); }
    private void updateActionButtonState() { runOnUiThread(() -> { if (aiButtonControl != null) { aiButtonControl.setEnabled(!isAiSequenceRunning); aiButtonControl.setAlpha(isAiSequenceRunning ? 0.5f : 1.0f); } }); }
    private void showGeminiResponse(String message) { runOnUiThread(() -> { if (!isFinishing()) new AlertDialog.Builder(this).setTitle("Gemini Response").setMessage(message).setPositiveButton("OK", null).show(); }); }
    private void showAiLogDialog() { if (aiInteractionLog.isEmpty()) { Toast.makeText(this, "AI Interaction Log is empty.", Toast.LENGTH_SHORT).show(); return; } AlertDialog.Builder builder = new AlertDialog.Builder(this); builder.setTitle("AI Interaction Log (Current Sequence)"); ScrollView scrollView = new ScrollView(this); TextView logTextView = new TextView(this); logTextView.setPadding(40, 40, 40, 40); logTextView.setText(TextUtils.join("\n\n---\n\n", aiInteractionLog)); logTextView.setTextIsSelectable(true); scrollView.addView(logTextView); builder.setView(scrollView); builder.setPositiveButton("Close", null); builder.show(); }


    // --- Activity Result Handling ---
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) { super.onActivityResult(requestCode, resultCode, data); if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) { if (currentTab == null) { createNewTab(); if (currentTab == null) { showError("Failed to create tab for file."); return; } } Uri uri = data.getData(); if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) { try { final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION); getContentResolver().takePersistableUriPermission(uri, takeFlags); } catch (SecurityException e) { Log.e(TAG, "SecurityException taking persistable URI permission.", e); } } if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) || ContentResolver.SCHEME_FILE.equals(uri.getScheme())) { handleContentUri(uri); } else { showError("Unsupported URI scheme from file picker: " + uri.getScheme()); } } }


    // --- Lifecycle Methods & Other Utils ---
    @Override public void onBackPressed() { if (isAiSequenceRunning) { endAiSequence(); Toast.makeText(this, "AI Sequence Cancelled", Toast.LENGTH_SHORT).show(); return; } if (isFullscreen) toggleFullscreen(); else if (currentTab != null && currentTab.webView != null && currentTab.webView.canGoBack()) currentTab.webView.goBack(); else if (tabs.size() > 1) closeTab(currentTab); else super.onBackPressed(); }
    @Override protected void onPause() { super.onPause(); endAiSequence(); removeGrid(); if (currentTab != null && currentTab.webView != null) currentTab.webView.onPause(); }
    @Override protected void onResume() { super.onResume(); if (currentTab != null && currentTab.webView != null) currentTab.webView.onResume(); updateActionButtonState(); }
    @Override protected void onDestroy() { endAiSequence(); removeGrid(); if (aiInteractionLog != null) aiInteractionLog.clear(); if (tabs != null) { for (Tab tab : tabs) { if (tab.webView != null) { ViewGroup parent = (ViewGroup) tab.webView.getParent(); if (parent != null) parent.removeView(tab.webView); tab.webView.stopLoading(); tab.webView.loadUrl("about:blank"); tab.webView.setWebViewClient(null); tab.webView.setWebChromeClient(null); tab.webView.destroy(); tab.webView = null; } } tabs.clear(); tabs = null; currentTab = null; } if (rootLayout != null) { rootLayout.removeAllViews(); rootLayout = null; } super.onDestroy(); }
    private void showMenu(View anchor) { ListPopupWindow popup = new ListPopupWindow(this); popup.setAnchorView(anchor); ArrayList<String> menuItems = new ArrayList<>(); menuItems.add("API Key Settings"); menuItems.add("Clone Tab"); menuItems.add("Open File..."); menuItems.add("Downloads"); menuItems.add("Find on Page"); menuItems.add(isDesktopMode ? "Switch to Mobile Mode" : "Switch to Desktop Mode"); String[] items = menuItems.toArray(new String[0]); ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) { @Override public View getView(int pos, View cv, ViewGroup p) { TextView v = (TextView) super.getView(pos, cv, p); v.setTextColor(0xFFFFFFFF); v.setBackgroundColor(0xFF000C2B); v.setPadding(40, 30, 40, 30); v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); return v; } }; popup.setAdapter(adapter); popup.setContentWidth(measureContentWidth(adapter)); popup.setModal(true); popup.setHeight(ListPopupWindow.WRAP_CONTENT); popup.setVerticalOffset(5); popup.setBackgroundDrawable(new ColorDrawable(0xEE000814)); popup.setOnItemClickListener((parent, view, position, id) -> { popup.dismiss(); String selectedItem = items[position]; switch (selectedItem) { case "API Key Settings": showApiKeyDialog(); break; case "Clone Tab": promptCloneTab(); break; case "Open File...": openFilePicker(); break; case "Downloads": showDownloads(); break; case "Find on Page": if (currentTab != null && currentTab.webView != null) showFindDialog(); break; case "Switch to Mobile Mode": case "Switch to Desktop Mode": toggleDesktopMode(); break; } }); popup.show(); }
    private void promptCloneTab() { if (currentTab == null) { showError("No active tab to clone."); return; } final String urlToClone = currentTab.getInitialUrl(); if (urlToClone == null || urlToClone.isEmpty() || urlToClone.equals(HOME_PAGE_URL)) { showError("Cannot clone empty or home pages."); return; } final EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER); input.setHint("Number of copies (1-10)"); AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Clone Tab").setMessage("Enter number of times to clone this tab's URL:\n" + urlToClone).setView(input).setPositiveButton("Clone", (d, which) -> { String numStr = input.getText().toString(); int numCopies = 0; try { numCopies = Integer.parseInt(numStr); } catch (NumberFormatException e) { /* Handled below */ } if (numCopies > 0 && numCopies <= 10) { for (int i = 0; i < numCopies; i++) createNewTab(urlToClone); } else { showError("Please enter a valid positive number (1-10)."); } hideKeyboard(input); }).setNegativeButton("Cancel", (d,w) -> hideKeyboard(input)).create(); dialog.setOnShowListener(di -> showKeyboard(input)); dialog.show(); }
    private int measureContentWidth(ListAdapter listAdapter) { ViewGroup mmp = null; int maxWidth = 0; View itemView = null; int itemType = 0; final int wms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED); final int hms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED); final int count = listAdapter.getCount(); for (int i = 0; i < count; i++) { final int pt = listAdapter.getItemViewType(i); if (pt != itemType) { itemType = pt; itemView = null; } if (mmp == null) mmp = new FrameLayout(this); itemView = listAdapter.getView(i, itemView, mmp); itemView.measure(wms, hms); final int iw = itemView.getMeasuredWidth(); if (iw > maxWidth) maxWidth = iw; } int cw = maxWidth + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()); int maw = (int) (getResources().getDisplayMetrics().widthPixels * 0.8); return Math.min(cw, maw); }
    private void openFilePicker() { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("*/*"); try { startActivityForResult(intent, FILE_PICKER_REQUEST_CODE); } catch (android.content.ActivityNotFoundException ex) { showError("No file manager app found to open files."); } }
    private void showDownloads() { if (downloadHistory.isEmpty()) { Toast.makeText(this, "Download history is empty (session)", Toast.LENGTH_SHORT).show(); return; } AlertDialog.Builder builder = new AlertDialog.Builder(this); builder.setTitle("Download History (Session)"); builder.setItems(downloadHistory.toArray(new String[0]), (dialog, which) -> { String url = downloadHistory.get(which).split(" \\(")[0]; android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); if (clipboard != null) { android.content.ClipData clip = android.content.ClipData.newPlainText("Downloaded URL", url); clipboard.setPrimaryClip(clip); Toast.makeText(MainActivity.this, "URL copied to clipboard", Toast.LENGTH_SHORT).show(); } }); builder.setPositiveButton("Close", null); builder.setNegativeButton("Clear Session History", (dialog, which) -> { downloadHistory.clear(); Toast.makeText(MainActivity.this, "Download history cleared", Toast.LENGTH_SHORT).show(); }); builder.show(); }
    private void showFindDialog() { if (currentTab == null || currentTab.webView == null) return; final FrameLayout container = new FrameLayout(this); final EditText input = new EditText(this); input.setHint("Enter text to find"); input.setSingleLine(true); FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); params.setMargins(50, 20, 50, 20); input.setLayoutParams(params); container.addView(input); AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Find on Page").setView(container).setPositiveButton("Next", null).setNegativeButton("Previous", null).setNeutralButton("Done", (d, w) -> { if (currentTab != null && currentTab.webView != null) currentTab.webView.clearMatches(); }).create(); dialog.setOnShowListener(di -> { Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE), btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE); WebView.FindListener findListener = (a, n, d) -> { if (d && currentTab != null && n == 0) Toast.makeText(MainActivity.this, "Text not found", Toast.LENGTH_SHORT).show(); }; if(currentTab != null && currentTab.webView != null) currentTab.webView.setFindListener(findListener); else { dialog.dismiss(); return; } btnPositive.setOnClickListener(v -> { if (!input.getText().toString().isEmpty() && currentTab != null && currentTab.webView != null) currentTab.webView.findNext(true); }); btnNegative.setOnClickListener(v -> { if (!input.getText().toString().isEmpty() && currentTab != null && currentTab.webView != null) currentTab.webView.findNext(false); }); input.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s, int st, int c, int a) {} public void onTextChanged(CharSequence s, int st, int b, int c) {} public void afterTextChanged(Editable s) { if (currentTab != null && currentTab.webView != null) currentTab.webView.findAllAsync(s.toString()); } }); if (input.getText().length() > 0 && currentTab != null && currentTab.webView != null) currentTab.webView.findAllAsync(input.getText().toString()); showKeyboard(input); }); dialog.setOnDismissListener(di -> { if (currentTab != null && currentTab.webView != null) { currentTab.webView.clearMatches(); currentTab.webView.setFindListener(null); } hideKeyboard(input); }); dialog.show(); }
    private void toggleDesktopMode() { if (currentTab == null || currentTab.webView == null) return; isDesktopMode = !isDesktopMode; WebSettings settings = currentTab.webView.getSettings(); settings.setUserAgentString(isDesktopMode ? DESKTOP_AGENT : MOBILE_AGENT); settings.setUseWideViewPort(isDesktopMode); settings.setLoadWithOverviewMode(isDesktopMode); Toast.makeText(this, "Switched to " + (isDesktopMode ? "Desktop" : "Mobile") + " Mode. Reloading.", Toast.LENGTH_LONG).show(); currentTab.webView.reload(); }
    private void loadUrlOrSearch(String input) {
        if (currentTab == null) { if(tabs.isEmpty()) createNewTab(); if(!tabs.isEmpty()) switchToTab(tabs.get(0)); if(currentTab == null) { showError("Cannot load URL: No active tab."); return; }}
        if (currentTab.webView == null) { showError("Cannot load URL: WebView not available."); return; }
        String url = input.trim(); if (url.isEmpty()) return;

        // Removed the explicit 'about:' check as it's no longer used for the home page
        // and other about: URIs are generally not standard/supported.
        if (url.startsWith("javascript:")) {
            currentTab.webView.loadUrl(url);
        } else if (url.startsWith("file:")) {
            // This now handles file:///android_asset/index.html via loadLocalFile
            loadLocalFile(url);
        } else if (url.startsWith("/") && new File(url).exists() && new File(url).isFile()) {
            loadLocalFile("file://" + url);
        } else if (url.startsWith("content:")) {
            handleContentUri(Uri.parse(url));
        } else {
            boolean looksLikeUrl = (url.contains(".") && !url.contains(" ")) || url.contains("://");
            String targetUrl;
            if (looksLikeUrl) {
                targetUrl = (!url.startsWith("http:") && !url.startsWith("https:") && !url.contains("://")) ? "https://" + url : url;
            } else {
                try {
                    targetUrl = SEARCH_ENGINE + Uri.encode(url);
                } catch (Exception e) {
                    showError("Invalid search term encoding"); return;
                }
            }
            currentTab.webView.loadUrl(targetUrl); urlBar.clearFocus();
        }
    }

} // End of MainActivity