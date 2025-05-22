export function parseCommand(commandString) {
  // Array that will contain the arguments
  const args = [];

  // Variable to store the argument being built
  let currentArg = '';

  // Indicates if we are inside a quoted string
  let inQuotes = false;

  // Quote character used (single or double)
  let quoteChar = '';

  // Indicates if the previous character was a backslash (for escaping)
  let escaped = false;

  // Loop through each character of the string
  for (let i = 0; i < commandString.length; i++) {
    const char = commandString[i];

    // Handle escaped characters
    if (escaped) {
      currentArg += char;
      escaped = false;
      continue;
    }

    // Detect escape characters
    if (char === '\\') {
      escaped = true;
      continue;
    }

    // Handle quotes
    if ((char === '"' || char === "'") && (inQuotes === false || quoteChar === char)) {
      if (!inQuotes) {
        inQuotes = true;
        quoteChar = char;
      } else {
        inQuotes = false;
        quoteChar = '';
      }
      continue;
    }

    // If it's a space and we're not in quotes, finalize the argument
    if (char === ' ' && !inQuotes) {
      if (currentArg) {
        args.push(currentArg);
        currentArg = '';
      }
    } else {
      // Otherwise add the character to the current argument
      currentArg += char;
    }
  }

  // Add the last argument if it exists
  if (currentArg) {
    args.push(currentArg);
  }

  return args;
}
