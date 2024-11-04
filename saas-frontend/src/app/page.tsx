'use client';

import Link from 'next/link'

export default function Home() {
  return (
      <div className="min-h-screen bg-gray-100 flex flex-col justify-center items-center">
        <h1 className="text-4xl font-bold mb-8">Bienvenue sur Notre SaaS</h1>
        <div className="space-x-4">
          <Link href="/login" className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded">
            Connexion
          </Link>
          <Link href="/signup" className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded">
            Inscription
          </Link>
        </div>
      </div>
  )
}
