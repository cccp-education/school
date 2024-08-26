const greeter = (person: string) => {
  return `Hello, ${person}!`;
}

function testGreeter() {
  const user = 'Grant';
  // @ts-ignore
  Logger.log(greeter(user));
}