import * as os from 'os';
import * as path from 'path';

export const tempDir = path.join(__dirname, 'spring-shell', 'temp');
export const isWindows = os.platform() === 'win32';
export const cliPathRelative = isWindows
  ? '..\\..\\spring-shell-samples\\spring-shell-sample-e2e\\build\\native\\nativeCompile\\spring-shell-sample-e2e.exe'
  : '../../spring-shell-samples/spring-shell-sample-e2e/build/native/nativeCompile/spring-shell-sample-e2e';
export const jarPathRelative = isWindows
  ? '..\\..\\spring-shell-samples\\spring-shell-sample-e2e\\build\\libs\\spring-shell-sample-e2e.jar'
  : '../../spring-shell-samples/spring-shell-sample-e2e/build/libs/spring-shell-sample-e2e.jar';
export const cliPath = path.resolve(cliPathRelative);
export const jarPath = path.resolve(jarPathRelative);
export const nativeDesc = 'native';
export const jarDesc = 'jar';
export const jarCommand = isWindows ? 'java.exe' : 'java';
export const nativeCommand = cliPath;
export const jarOptions = ['-jar', jarPath];
export const waitForExpectDefaultTimeout = 30000;
export const waitForExpectDefaultInterval = 2000;
export const testTimeout = 120000;
