import Link from 'next/link'

export default function Dashboard() {
    return (
        <div className="min-h-screen bg-gray-100">
            <div className="container mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold mb-8">Tableau de bord</h1>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-semibold mb-4">Statistiques</h2>
                        <p>Utilisateurs actifs : 100</p>
                        <p>Projets en cours : 25</p>
                    </div>
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-semibold mb-4">Tâches récentes</h2>
                        <ul className="list-disc list-inside">
                            <li>Mise à jour du profil</li>
                            <li>Création d'un nouveau projet</li>
                            <li>Invitation d'un collaborateur</li>
                        </ul>
                    </div>
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-semibold mb-4">Liens rapides</h2>
                        <ul className="space-y-2">
                            <li>
                                <Link href="/profile" className="text-blue-500 hover:underline">
                                    Profil
                                </Link>
                            </li>
                            <li>
                                <Link href="/settings" className="text-blue-500 hover:underline">
                                    Paramètres
                                </Link>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    )
}