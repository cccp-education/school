import { render, screen } from '@testing-library/react'
import Navigation from '../Navigation'

describe('Navigation', () => {
    it('renders the navigation links', () => {
        render(<Navigation />)
        const dashboardLink = screen.getByRole('link', { name: /Tableau de bord/i })
        const profileLink = screen.getByRole('link', { name: /Profil/i })
        const settingsLink = screen.getByRole('link', { name: /Paramètres/i })
        const logoutLink = screen.getByRole('link', { name: /Déconnexion/i })

        expect(dashboardLink).toBeInTheDocument()
        expect(profileLink).toBeInTheDocument()
        expect(settingsLink).toBeInTheDocument()
        expect(logoutLink).toBeInTheDocument()
    })

    it('renders the app name', () => {
        render(<Navigation />)
        const appName = screen.getByText(/SaaS App/i)
        expect(appName).toBeInTheDocument()
    })
})