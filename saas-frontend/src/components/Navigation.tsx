import Link from 'next/link'

export default function Navigation() {
    return (
        <nav className="bg-gray-800">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    <div className="flex items-center">
                        <Link href="/" className="text-white font-bold text-xl">
                            SaaS App
                        </Link>
                        <div className="ml-10 flex items-baseline space-x-4">
                            <Link href="/dashboard" className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium">
                                Tableau de bord
                            </Link>
                            <Link href="/profile" className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium">
                                Profil
                            </Link>
                            <Link href="/settings" className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium">
                                Paramètres
                            </Link>
                        </div>
                    </div>
                    <div>
                        <Link href="/login" className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium">
                            Déconnexion
                        </Link>
                    </div>
                </div>
            </div>
        </nav>
    )
}