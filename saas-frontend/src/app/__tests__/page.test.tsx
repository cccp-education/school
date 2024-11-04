import '@testing-library/jest-dom';
import '@testing-library/jest-dom/jest-globals';
import {describe, expect, it} from "@jest/globals";
import {render, screen} from '@testing-library/react';
import Home from '../page'

describe('Home', () => {
    it('renders the welcome message', () => {
        render(<Home/>)
        const heading = screen.getByRole('heading', {name: /Bienvenue sur Notre SaaS/i})
        expect(heading).toBeInTheDocument()
    })

    it('renders the login and signup buttons', () => {
        render(<Home/>)
        const loginButton = screen.getByRole('link', {name: /Connexion/i})
        const signupButton = screen.getByRole('link', {name: /Inscription/i})
        expect(loginButton).toBeInTheDocument()
        expect(signupButton).toBeInTheDocument()
    })
})