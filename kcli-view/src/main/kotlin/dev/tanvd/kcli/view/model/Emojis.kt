package dev.tanvd.kcli.view.model

@Suppress("unused")
enum class Emojis(val symbol: String, val description: String) {

    // Smileys & Emotion
    GRINNING_FACE("😀", "Grinning face"),
    SLIGHTLY_SMILING_FACE("🙂", "Slightly smiling face"),
    NEUTRAL_FACE("😐", "Neutral face"),
    THINKING_FACE("🤔", "Thinking face"),
    FACE_WITH_RAISED_EYEBROW("🤨", "Skeptical / questioning"),
    HUSHED_FACE("😯", "Surprised / unexpected result"),
    FACE_SCREAMING("😱", "Critical error / shock"),
    ROBOT("🤖", "Bot / automated process"),

    // People & Body
    WAVING_HAND("👋", "Hello / goodbye"),
    THUMBS_UP("👍", "Approval / success"),
    THUMBS_DOWN("👎", "Disapproval / failure"),
    CLAPPING_HANDS("👏", "Completion / applause"),
    RAISING_HANDS("🙌", "Celebration"),
    POINT_RIGHT("👉", "Directing attention right"),
    POINT_LEFT("👈", "Directing attention left"),
    POINT_UP("☝️", "Note / important"),
    BRAIN("🧠", "Intelligence / processing"),
    EYES("👀", "Watching / monitoring"),
    EYE("👁️", "Observing"),

    // Animals & Nature
    BUG("🐛", "Bug / defect"),
    SEEDLING("🌱", "New / growing / first-time"),
    EARTH_AFRICA("🌍", "Global / worldwide"),
    EARTH_AMERICAS("🌎", "Global / worldwide"),
    EARTH_ASIA("🌏", "Global / worldwide"),

    // Travel & Places
    ROCKET("🚀", "Launch / deploy / fast"),
    CONSTRUCTION("🚧", "Work in progress"),
    BUILDING_CONSTRUCTION("🏗️", "Building / scaffolding"),

    // Activities
    PARTY_POPPER("🎉", "Celebration / major success"),
    TROPHY("🏆", "Achievement / winner"),
    MEDAL("🥇", "First place / best result"),
    PUZZLE_PIECE("🧩", "Integration / missing piece"),

    // Objects — Devices
    LAPTOP("💻", "Development / coding"),
    DESKTOP_COMPUTER("🖥️", "Server / desktop"),
    KEYBOARD("⌨️", "Terminal / input"),
    MOBILE_PHONE("📱", "Mobile / device"),
    BATTERY("🔋", "Power / energy level"),
    ELECTRIC_PLUG("🔌", "Connection / plugin"),
    SATELLITE_ANTENNA("📡", "Network / transmission"),

    // Objects — Office & Documents
    PACKAGE("📦", "Package / bundle / release"),
    INBOX_TRAY("📥", "Input / receive"),
    OUTBOX_TRAY("📤", "Output / send"),
    ENVELOPE("✉️", "Email / message"),
    OPEN_MAILBOX("📬", "New mail / notification"),
    CLIPBOARD("📋", "List / summary"),
    FILE_FOLDER("📁", "Directory"),
    OPEN_FILE_FOLDER("📂", "Open directory"),
    PAGE("📄", "File / document"),
    BOOKMARK_TABS("📑", "Index / contents"),
    OPEN_BOOK("📖", "Reading / documentation"),
    BOOKS("📚", "Library / reference"),
    MEMO("📝", "Notes / editing"),
    PENCIL("✏️", "Edit"),
    FOUNTAIN_PEN("🖊️", "Write"),
    WASTEBASKET("🗑️", "Delete / discard"),

    // Objects — Tools
    HAMMER("🔨", "Build"),
    HAMMER_AND_WRENCH("🛠️", "Tools / fix"),
    WRENCH("🔧", "Configure / fix"),
    NUT_AND_BOLT("🔩", "Assemble / details"),
    GEAR("⚙️", "Settings / configuration"),
    TOOLBOX("🧰", "Utilities / toolkit"),
    LINK("🔗", "Link / connection"),
    CHAINS("⛓️", "Dependency / binding"),
    SCISSORS("✂️", "Cut / trim"),
    CARD_INDEX("🗂️", "Index / catalog"),

    // Objects — Science
    MICROSCOPE("🔬", "Detailed analysis / test"),
    TELESCOPE("🔭", "Exploration / search"),
    TEST_TUBE("🧪", "Experiment / test"),
    MAGNET("🧲", "Attract / pull"),
    DNA("🧬", "Genetics / mutation"),

    // Objects — Security
    LOCK("🔒", "Locked / authenticated"),
    UNLOCK("🔓", "Unlocked / open"),
    KEY("🔑", "Key / credential / token"),
    SHIELD("🛡️", "Protection / security"),

    // Objects — Data & Finance
    FLOPPY_DISK("💾", "Save"),
    OPTICAL_DISK("💿", "Storage"),
    BAR_CHART("📊", "Statistics / metrics"),
    CHART_INCREASING("📈", "Growth / improvement"),
    CHART_DECREASING("📉", "Decline / regression"),
    LIGHT_BULB("💡", "Tip / idea / suggestion"),

    // Objects — Time
    HOURGLASS_FLOWING("⏳", "In progress / loading"),
    HOURGLASS_DONE("⌛", "Time is up / timeout"),
    STOPWATCH("⏱️", "Elapsed time / benchmark"),
    ALARM_CLOCK("⏰", "Scheduled / deadline"),
    SPIRAL_CALENDAR("🗓️", "Date / planning"),

    // Objects — Communication
    SPEECH_BALLOON("💬", "Chat / comment"),
    THOUGHT_BALLOON("💭", "Thinking / processing"),
    LOUDSPEAKER("📢", "Announcement"),
    BELL("🔔", "Notification / alert"),
    BELL_SLASH("🔕", "Silent / muted"),
    MAGNIFYING_GLASS_LEFT("🔍", "Search"),
    MAGNIFYING_GLASS_RIGHT("🔎", "Inspect / zoom in"),

    // Symbols — Status
    CHECK_MARK("✅", "Success / done"),
    CROSS_MARK("❌", "Failure / error"),
    WARNING("⚠️", "Warning"),
    INFORMATION("ℹ️", "Information"),
    EXCLAMATION("❗", "Important / urgent"),
    QUESTION("❓", "Unknown / question"),
    HOLLOW_RED_CIRCLE("⭕", "Required / must"),
    PROHIBITED("🚫", "Prohibited / blocked"),
    NO_ENTRY("⛔", "Stop / forbidden"),

    // Symbols — Colored circles
    RED_CIRCLE("🔴", "Error / offline / stop"),
    ORANGE_CIRCLE("🟠", "Warning / degraded"),
    YELLOW_CIRCLE("🟡", "Caution / pending"),
    GREEN_CIRCLE("🟢", "OK / online / go"),
    BLUE_CIRCLE("🔵", "Info / neutral"),
    PURPLE_CIRCLE("🟣", "Special / custom"),
    BLACK_CIRCLE("⚫", "Disabled / unknown"),
    WHITE_CIRCLE("⚪", "Empty / none"),

    // Symbols — Arrows
    RIGHT_ARROW("➡️", "Next / forward"),
    LEFT_ARROW("⬅️", "Back / return"),
    UP_ARROW("⬆️", "Up / upload / increase"),
    DOWN_ARROW("⬇️", "Down / download / decrease"),
    UP_RIGHT_ARROW("↗️", "Rising / expanding"),
    DOWN_RIGHT_ARROW("↘️", "Falling / shrinking"),
    LEFT_RIGHT_ARROW("↔️", "Bidirectional / range"),
    UP_DOWN_ARROW("↕️", "Vertical / resize"),
    COUNTERCLOCKWISE("🔄", "Refresh / retry / rotate"),
    FAST_FORWARD("⏩", "Skip / fast"),
    REWIND("⏪", "Undo / back"),

    // Symbols — Other
    SPARKLES("✨", "New / highlight / magic"),
    STAR("⭐", "Favourite / starred"),
    GLOWING_STAR("🌟", "Outstanding / featured"),
    RECYCLING("♻️", "Reuse / recycle / cleanup"),
    NEW_BUTTON("🆕", "New"),
    FREE_BUTTON("🆓", "Free"),
    UP_BUTTON("🆙", "Updated / upgraded"),
    TRIDENT("🔱", "Powerful / advanced"),
}
