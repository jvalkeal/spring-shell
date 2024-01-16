import 'jest-extended';
import waitForExpect from 'wait-for-expect';
import { Cli } from 'spring-shell-e2e';
import {
  nativeDesc,
  jarDesc,
  jarCommand,
  nativeCommand,
  jarOptions,
  waitForExpectDefaultTimeout,
  waitForExpectDefaultInterval,
  testTimeout
} from '../src/utils';

/**
 * test for version command returns expected info
 */
const versionReturnsInfoDesc = 'version returns info';
const versionCommand = ['version'];
const versionReturnsInfo = async (cli: Cli) => {
  cli.run();
  await waitForExpect(async () => {
    const screen = cli.screen();
    expect(screen).toEqual(expect.arrayContaining([expect.stringContaining('Build Version')]));
  });
  await expect(cli.exitCode()).resolves.toBe(0);
};

/**
 * test for command help command returns expected info
 */
 const commandHelpReturnsInfoDesc = 'command help returns info';
 const commandHelpCommand = ['help', 'help'];
 const commandHelpReturnsInfo = async (cli: Cli) => {
   cli.run();
   await waitForExpect(async () => {
     const screen = cli.screen();
     expect(screen).toEqual(expect.arrayContaining([expect.stringContaining('OPTIONS')]));
     expect(screen).not.toEqual(expect.arrayContaining([expect.stringContaining('CURRENTLY UNAVAILABLE')]));
   });
   await expect(cli.exitCode()).resolves.toBe(0);
 };

/**
 * test for command help succeed
 */
const helpReturnsInfoDesc = 'command help returns info';
const helpCommand = ['help'];
const helpReturnsInfo = async (cli: Cli) => {
  cli.run();
  await waitForExpect(async () => {
    const screen = cli.screen();
    expect(screen).toEqual(expect.arrayContaining([expect.stringContaining('AVAILABLE COMMANDS')]));
  });
  await expect(cli.exitCode()).resolves.toBe(0);
};

// all builtin commands
describe('builtin commands', () => {

  let cli: Cli;

  beforeEach(async () => {
    waitForExpect.defaults.timeout = waitForExpectDefaultTimeout;
    waitForExpect.defaults.interval = waitForExpectDefaultInterval;
  }, testTimeout);

  afterEach(async () => {
    cli?.dispose();
  }, testTimeout);

  describe.each(
    [
      { groupDesc: jarDesc, groupCommand: jarCommand, groupOptions: jarOptions, groupData: [
          { testDesc: versionReturnsInfoDesc, testFn: versionReturnsInfo, testCommand: versionCommand },
          { testDesc: commandHelpReturnsInfoDesc, testFn: commandHelpReturnsInfo, testCommand: commandHelpCommand },
          { testDesc: helpReturnsInfoDesc, testFn: helpReturnsInfo, testCommand: helpCommand },
        ]
      },
      { groupDesc: nativeDesc, groupCommand: nativeCommand, groupOptions: [], groupData: [
          { testDesc: versionReturnsInfoDesc, testFn: versionReturnsInfo, testCommand: versionCommand },
          { testDesc: commandHelpReturnsInfoDesc, testFn: commandHelpReturnsInfo, testCommand: commandHelpCommand },
          { testDesc: helpReturnsInfoDesc, testFn: helpReturnsInfo, testCommand: helpCommand },
        ]
      },
    ]
  )('$groupDesc', ({groupCommand, groupOptions, groupData}) => {
      
    it.each(groupData)(
      '$testDesc',
      async ({testFn, testCommand}) => {
        cli = new Cli({
            command: groupCommand,
            options: [...groupOptions, ...testCommand]
          });  
        await testFn(cli);
      },
      testTimeout
    );

  });

});

