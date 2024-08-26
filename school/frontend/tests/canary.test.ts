import { expect, test } from '@playwright/test';

test.describe('Canary Test', () => {
  test('should pass this canary test', async () => {
    expect(true).toBe(true);
  });
});
