import { test, expect } from '@playwright/test'

test.describe('Smoke tests', () => {

  test.describe('Admin Login', () => {
    test('should display login page', async ({ page }) => {
      await page.goto('/admin/login')
      await expect(page).toHaveTitle(/Login|登录/)
    })

    test('should reject empty form submission', async ({ page }) => {
      await page.goto('/admin/login')
      await page.click('button:has-text("登录")')
      // Should still be on login page (form validation prevents submit)
      await expect(page).toHaveURL(//admin/login/)
    })
  })

  test.describe('Public API', () => {
    test('health endpoint should return 200', async ({ request }) => {
      const resp = await request.get('http://localhost:8080/actuator/health')
      expect(resp.ok()).toBeTruthy()
    })
  })
})
