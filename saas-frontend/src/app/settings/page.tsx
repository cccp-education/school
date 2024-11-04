'use client'

import { useState } from 'react'

export default function Settings() {
    const [emailNotifications, setEmailNotifications] = useState(true)
    const [darkMode, setDarkMode] = useState(false)
    const [language, setLanguage] = useState('fr')

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()
        // Ici, vous ajouteriez la logique de mise à jour des paramètres
        console.log('Mise à jour des paramètres:', { emailNotifications, darkMode, language })
    }

    return (
        <div className="min-h-screen bg-gray-100">
            <div className="container mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold mb-8">Paramètres</h1>
                <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
                    <div className="mb-4">
                        <label className="flex items-center">
                            <input
                                type="checkbox"
                                checked={emailNotifications}
                                onChange={(e) => setEmailNotifications(e.target.checked)}
                                className="mr-2"
                            />
                            <span className="text-gray-700">Recevoir des notifications par email</span>
                        </label>
                    </div>
                    <div className="mb-4">
                        <label className="flex items-center">
                            <input
                                type="checkbox"
                                checked={darkMode}
                                onChange={(e) => setDarkMode(e.target.checked)}
                                className="mr-2"
                            />
                            <span className="text-gray-700">Mode sombre</span>
                        </label>
                    </div>
                    <div className="mb-6">
                        <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="language">
                            Langue
                        </label>
                        <select
                            id="language"
                            value={language}
                            onChange={(e) => setLanguage(e.target.value)}
                            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        >
                            <option value="fr">Français</option>
                            <option value="en">English</option>
                            <option value="es">Español</option>
                        </select>
                    </div>
                    <div className="flex items-center justify-between">
                        <button
                            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"

                            type="submit"
                        >
                            Enregistrer les paramètres
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}