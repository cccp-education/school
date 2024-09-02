<body class="font-sans antialiased">
<div id="app" class="min-h-screen transition-colors duration-300">
    <header class="bg-white dark:bg-gray-800 shadow-md">
        <nav class="container mx-auto px-4 py-4 flex justify-between items-center">
            <a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html" class="text-2xl font-bold text-primary dark:text-white">Talaria-Formation</a>
            <div class="flex items-center space-x-4">
                <button id="themeToggle" class="p-2 rounded-full bg-gray-200 dark:bg-gray-600">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                         stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/>
                    </svg>
                </button>
                <select id="languageSelector"
                        class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded px-2 py-1">
                    <option value="fr">Français</option>
                    <option value="en">English</option>
                    <option value="es">Español</option>
                </select>
            </div>
        </nav>
    </header>
