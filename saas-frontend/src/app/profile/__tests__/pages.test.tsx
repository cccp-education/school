import { render, screen, fireEvent } from '@testing-library/react'
import Login from '../page'
import {describe, expect, it} from "@jest/globals";

jest.mock('next/navigation', () => ({
    useRouter() {
        return {
            push: jest.fn(),
        }
    },
}))

describe('Login', () => {
    it('renders the login form', () => {
        render(<Login />)
        const emailInput = screen.getByLabelText(/Email/i)
        const passwordInput = screen.getByLabelText(/Mot de passe/i)
        const submitButton = screen.getByRole('button', { name: /Se connecter/i })
        expect(emailInput).toBeInTheDocument()
        expect(passwordInput).toBeInTheDocument()
        expect(submitButton).toBeInTheDocument()
    })

    it('handles form submission', () => {
        const { push } = require('next/navigation').useRouter()
        render(<Login />)
        const emailInput = screen.getByLabelText(/Email/i)
        const passwordInput = screen.getByLabelText(/Mot de passe/i)
        const submitButton = screen.getByRole('button', { name: /Se connecter/i })

        fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
        fireEvent.change(passwordInput, { target: { value: 'password123' } })
        fireEvent.click(submitButton)

        expect(push).toHaveBeenCalledWith('/dashboard')
    })
})