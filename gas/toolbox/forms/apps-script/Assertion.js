/*
```javascript
// Exemple d'utilisation
const someFunction = () => {
    throw new TypeError('This is a test error');
};

Assertion.assertEquals(2 + 2, 5, 'Adding numbers');
Assertion.assertTrue(2 + 2 === 4, 'Adding numbers');
Assertion.assertFalse(2 + 2 === 5, 'Adding numbers');
Assertion.assertUndefined(undefinedVariable, 'Undefined variable check');
Assertion.assertNotNull(nullValue, 'Null value check');
Assertion.assertNotNaN('Not a number', 'NaN check');
Assertion.expectedException(someFunction, TypeError, 'Testing expected exception');
```
*/
const Assertion = {
    assertEquals: (actual, expected, log, message = '') => {
        if (actual !== expected) log(`Assertion Failed: ${message}. Expected ${expected}, but got ${actual}`);
    },
    assertTrue: (condition, log, message = '') => {
        if (!condition) log(`Assertion Failed: ${message}. Expected true, but got false`);
    },
    assertFalse: (condition, log, message = '') => {
        if (condition) log(`Assertion Failed: ${message}. Expected false, but got true`);
    },
    assertUndefined: (value, log, message = '') => {
        if (typeof value !== 'undefined') log(`Assertion Failed: ${message}. Expected undefined, but got ${value}`);
    },
    assertNotNull: (value, log, message = '') => {
        if (value === null) log(`Assertion Failed: ${message}. Expected not null, but got null`);
    },
    assertNotNaN: (value, log, message = '') => {
        if (isNaN(value)) log(`Assertion Failed: ${message}. Expected not NaN, but got NaN`);
    },
    expectedException: (func, expectedError, log, message = '') => {
        try {
            func();
            log(`Assertion Failed: ${message}. Expected an exception of type ${expectedError}`);
        } catch (error) {
            if (!(error instanceof expectedError)) log(`Assertion Failed: ${message}. Expected an exception of type ${expectedError}, but got ${error}`);
        }
    }
};
