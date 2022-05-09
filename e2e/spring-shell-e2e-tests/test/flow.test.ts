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

// all flow commands
describe('flow commands', () => {
  let cli: Cli;
  let command: string;
  let options: string[] = [];

  /**
   * test for flow conditional field2 skips field1
   */
  const flowConditionalField2SkipsFields1Desc = 'flow conditional field2 skips field1';
  const flowConditionalCommand = ['flow', 'conditional'];
  const flowConditionalField2SkipsFields1 = async (cli: Cli) => {
    cli.run();

    // windows on gh actions may take quite a bit of time to start
    // so use long timeout
    // console.log('cli run 1');
    await waitForExpect(async () => {
      const screen = cli.screen();
      // console.log(screen);
      expect(screen).toEqual(expect.arrayContaining([expect.stringContaining('Single1')]));
    }, 60000);
    // console.log('cli run 2');

    await cli.keyDown();
    await waitForExpect(async () => {
      const screen = cli.screen();
      expect(screen).toEqual(expect.arrayContaining([expect.stringContaining('> Field2')]));
    });

    await cli.keyEnter();
    await waitForExpect(async () => {
      const screen = cli.screen();
      expect(screen).toEqual(
        expect.arrayContaining([expect.stringContaining('? Field2 [Default defaultField2Value]')])
      );
    });

    await cli.keyEnter();
    await waitForExpect(async () => {
      const screen = cli.screen();
      expect(screen).toEqual(expect.arrayContaining([expect.stringContaining('Field2 defaultField2Value')]));
    });

    await expect(cli.exitCode()).resolves.toBe(0);
  };

  beforeEach(async () => {
    waitForExpect.defaults.timeout = waitForExpectDefaultTimeout;
    waitForExpect.defaults.interval = waitForExpectDefaultInterval;
  }, testTimeout);

  afterEach(async () => {
    cli?.dispose();
  }, testTimeout);

  /**
   * fatjar commands
   */
  describe(jarDesc, () => {
    // console.log('ba1');
    beforeAll(() => {
      command = jarCommand;
      options = jarOptions;
    });

    it(
      flowConditionalField2SkipsFields1Desc,
      async () => {
        // console.log('it1');
        cli = new Cli({
          command: command,
          options: [...options, ...flowConditionalCommand]
        });
        await flowConditionalField2SkipsFields1(cli);
      },
      testTimeout
    );
  });

  /**
   * native commands
   */
   describe(nativeDesc, () => {
    beforeAll(() => {
      // console.log('ba2');
      command = nativeCommand;
      options = [];
    });

    it(
      flowConditionalField2SkipsFields1Desc,
      async () => {
        // console.log('it2');
        cli = new Cli({
          command: command,
          options: [...options, ...flowConditionalCommand]
        });
        await flowConditionalField2SkipsFields1(cli);
      },
      testTimeout
    );
  });
});
