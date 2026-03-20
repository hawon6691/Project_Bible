import { spawn } from "node:child_process";

function defaultCommand(command) {
  if (command) {
    return command;
  }

  return process.platform === "win32" ? "npm.cmd" : "npm";
}

export function runCommand(args, options = {}) {
  const startedAt = Date.now();
  const command = defaultCommand(options.command);
  const isCmdScript = /\.cmd$/i.test(command);
  const useShell = options.shell ?? false;
  const spawnCommand = isCmdScript ? (process.env.ComSpec ?? "cmd.exe") : command;
  const spawnArgs = isCmdScript
    ? ["/d", "/s", "/c", `${command} ${args.join(" ")}`]
    : args;

  return new Promise((resolve, reject) => {
    const child = spawn(spawnCommand, spawnArgs, {
      cwd: options.cwd ?? process.cwd(),
      env: options.env ?? process.env,
      stdio: options.stdio ?? "inherit",
      shell: useShell,
    });

    child.on("error", reject);
    child.on("close", (code) => {
      resolve({
        code: code ?? 1,
        durationMs: Date.now() - startedAt,
        command,
        args,
      });
    });
  });
}
