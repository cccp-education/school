<footer class="bg-gray-200 dark:bg-gray-800 mt-12 py-8">
    <div class="container mx-auto px-4 text-center text-gray-600 dark:text-gray-300">
        <div class="container mx-auto px-8">
            <div class="w-full flex flex-col md:flex-row py-4">
                <div class="flex-1">
                    <p class="uppercase text-gray-500 md:mb-6">Links</p>
                    <ul class="list-reset mb-6">
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#"
                               class="no-underline hover:underline text-gray-800 hover:text-pink-500">FAQ</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#"
                               class="no-underline hover:underline text-gray-800 hover:text-pink-500">Help</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Support</a>
                        </li>
                    </ul>
                </div>
                <div class="flex-1">
                    <p class="uppercase text-gray-500 md:mb-6">Legal</p>
                    <ul class="list-reset mb-6">
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#"
                               class="no-underline hover:underline text-gray-800 hover:text-pink-500">Terms</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Privacy</a>
                        </li>
                    </ul>
                </div>
                <div class="flex-1">
                    <p class="uppercase text-gray-500 md:mb-6">Social</p>
                    <ul class="list-reset mb-6">
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Facebook</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Linkedin</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Twitter</a>
                        </li>
                    </ul>
                </div>
                <div class="flex-1">
                    <p class="uppercase text-gray-500 md:mb-6">Company</p>
                    <ul class="list-reset mb-6">
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Official
                                Blog</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">About
                                Us</a>
                        </li>
                        <li class="mt-2 inline-block mr-2 md:block md:mr-0">
                            <a href="#" class="no-underline hover:underline text-gray-800 hover:text-pink-500">Contact</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</footer>

</div>

<script>
    // Internationalisation
    const translations = {
        fr: {
            "hero.title": "Bienvenue chez Talaria-Formation",
            "hero.subtitle": "Votre partenaire pour une formation de qualité",
            "features.course1": "Cours en ligne",
            "features.course1Description": "Accédez à nos cours en ligne de haute qualité, disponibles 24/7.",
            "features.course2": "Ateliers pratiques",
            "features.course2Description": "Participez à nos ateliers pratiques pour une expérience d'apprentissage immersive.",
            "features.course3": "Certifications",
            "features.course3Description": "Obtenez des certifications reconnues dans votre domaine d'expertise.",
            "cta.contact": "Contactez-nous",
            "footer.rights": "Tous droits réservés.",
            "landing.button.catalogue": "Catalogue",
            "formation.prepro_cda.name": "Prépro CDA",
            "formation.prepro_cda.summary": "Pré-professionnalisation au métier de Concepteur Développeur d'Application",
        },
        en: {
            "hero.title": "Welcome to Talaria-Formation",
            "hero.subtitle": "Your partner for quality training",
            "features.course1": "Online Courses",
            "features.course1Description": "Access our high-quality online courses, available 24/7.",
            "features.course2": "Practical Workshops",
            "features.course2Description": "Participate in our practical workshops for an immersive learning experience.",
            "features.course3": "Certifications",
            "features.course3Description": "Obtain recognized certifications in your field of expertise.",
            "cta.contact": "Contact Us",
            "footer.rights": "All rights reserved.",
            "landing.button.catalogue": "Catalogue",
            "formation.prepro_cda.name": "Prepro-ADD",
            "formation.prepro_cda.summary": "Pre-professionalization for the profession of Application Designer Developer",
        },
        es: {
            "hero.title": "Bienvenido a Talaria-Formation",
            "hero.subtitle": "Tu socio para una formación de calidad",
            "features.course1": "Cursos en línea",
            "features.course1Description": "Accede a nuestros cursos en línea de alta calidad, disponibles 24/7.",
            "features.course2": "Talleres prácticos",
            "features.course2Description": "Participa en nuestros talleres prácticos para una experiencia de aprendizaje inmersiva.",
            "features.course3": "Certificaciones",
            "features.course3Description": "Obtén certificaciones reconocidas en tu campo de especialización.",
            "cta.contact": "Contáctanos",
            "footer.rights": "Todos los derechos reservados.",
            "landing.button.catalogue": "Catálogo",
            "formation.prepro_cda.name": "Prepro-DDA",
            "formation.prepro_cda.summary": "Preprofesionalización para la profesión de Desarrollador Diseñador de Aplicaciones",
        }
    };

    function setLanguage(lang) {
        document.querySelectorAll('[data-i18n]').forEach(element => {
            const key = element.getAttribute('data-i18n');
            element.textContent = translations[lang][key] || key;
        });
        document.documentElement.lang = lang;
    }

    const languageSelector = document.getElementById('languageSelector');
    languageSelector.addEventListener('change', (e) => setLanguage(e.target.value));

    // Theme toggling
    const themeToggle = document.getElementById('themeToggle');
    const app = document.getElementById('app');
    let isDarkMode = localStorage.getItem('darkMode') === 'true';

    function applyTheme() {
        document.documentElement.classList.toggle('dark', isDarkMode);
        localStorage.setItem('darkMode', isDarkMode);
    }

    function toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    themeToggle.addEventListener('click', toggleTheme);

    // Initialize theme
    applyTheme();

    // Initialize language
    setLanguage('fr');
</script>
</body>
</html>
