<main class="container mx-auto px-4 py-8 bg-white dark:bg-gray-900">
    <section class="text-center mb-12">
        <h1 class="text-3xl md:text-4xl lg:text-5xl font-bold mb-4 text-gray-900 dark:text-white"
            data-i18n="hero.title">Bienvenue chez Talaria-Formation</h1>
        <p class="text-lg md:text-xl text-gray-600 dark:text-gray-300" data-i18n="hero.subtitle">Votre partenaire
            pour une formation de qualité</p>
    </section>

    <section class="text-center">
        <a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>formations/index.html"
           class="inline-block bg-primary hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-lg transition duration-300"
           data-i18n="landing.button.catalogue">Catalogue</a>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-12">
        <div class="bg-gray-100 dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 class="text-2xl font-semibold mb-4 text-primary dark:text-secondary" data-i18n="features.course1">
                Cours en ligne</h2>
            <p class="text-gray-600 dark:text-gray-300" data-i18n="features.course1Description">Accédez à nos cours
                en ligne de haute qualité, disponibles 24/7.</p>
        </div>
        <div class="bg-gray-100 dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 class="text-2xl font-semibold mb-4 text-primary dark:text-secondary" data-i18n="features.course2">
                Ateliers pratiques</h2>
            <p class="text-gray-600 dark:text-gray-300" data-i18n="features.course2Description">Participez à nos
                ateliers pratiques pour une expérience d'apprentissage immersive.</p>
        </div>
        <div class="bg-gray-100 dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 class="text-2xl font-semibold mb-4 text-primary dark:text-secondary" data-i18n="features.course3">
                Certifications</h2>
            <p class="text-gray-600 dark:text-gray-300" data-i18n="features.course3Description">Obtenez des
                certifications reconnues dans votre domaine d'expertise.</p>
        </div>
    </section>

</main>
