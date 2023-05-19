/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.samples.view;

import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.Screen;
import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.View.Rectangle;
import org.springframework.shell.component.view.eventloop.EventLoop;
import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.VerticalAlign;
import org.springframework.shell.component.view.message.ShellMessageHeaderAccessor;
import org.springframework.shell.component.view.message.StaticShellMessageHeaderAccessor;
import org.springframework.shell.standard.AbstractShellComponent;


/**
 * Commands showing some features under "view showcase".
 *
 * @author Janne Valkealahti
 */
@Command(command = { "view", "showcase" })
public class ViewShowcaseCommands extends AbstractShellComponent {

	private final static Logger log = LoggerFactory.getLogger(ViewShowcaseCommands.class);

	@Command(command = "clock")
	public void clock() {
		// setup handler with one box view
		TerminalUI component = new TerminalUI(getTerminal());

		// simply use a plain box view to draw a date using a custom
		// draw function
		BoxView root = new BoxView();
		root.setTitle("What's o'clock");
		root.setShowBorder(true);

		// store text to print
		AtomicReference<String> ref = new AtomicReference<>();

		// dispatch dates as messages
		Flux<Message<?>> dates = Flux.interval(Duration.ofSeconds(1)).map(l -> {
			String date = new Date().toString();
			Message<String> message = MessageBuilder
				.withPayload(date)
				.setHeader(ShellMessageHeaderAccessor.EVENT_TYPE, EventLoop.Type.USER)
				.build();
			return message;
		});
		component.getEventLoop().dispatch(dates);

		// process dates
		component.getEventLoop().events()
			.filter(m -> EventLoop.Type.USER.equals(StaticShellMessageHeaderAccessor.getEventType(m)))
			.doOnNext(message -> {
				if(message.getPayload() instanceof String s) {
					ref.set(s);
					component.redraw();
				}
			})
			.subscribe();

		// testing for animations for now
		AtomicInteger animX = new AtomicInteger();
		component.getEventLoop().events()
			.filter(m -> EventLoop.Type.SYSTEM.equals(StaticShellMessageHeaderAccessor.getEventType(m)))
			.filter(m -> m.getHeaders().containsKey("animationtick"))
			.doOnNext(message -> {
				Object payload = message.getPayload();
				if (payload instanceof Integer i) {
					animX.set(i);
					component.redraw();
				}
			})
			.subscribe();

		AtomicReference<HorizontalAlign> hAlign = new AtomicReference<>(HorizontalAlign.CENTER);
		AtomicReference<VerticalAlign> vAlign = new AtomicReference<>(VerticalAlign.CENTER);

		// handle keys
		component.getEventLoop().keyEvents()
			.doOnNext(e -> {
				switch (e.key()) {
					case DOWN:
						if (vAlign.get() == VerticalAlign.TOP) {
							vAlign.set(VerticalAlign.CENTER);
						}
						else if (vAlign.get() == VerticalAlign.CENTER) {
							vAlign.set(VerticalAlign.BOTTOM);
						}
						break;
					case UP:
						if (vAlign.get() == VerticalAlign.BOTTOM) {
							vAlign.set(VerticalAlign.CENTER);
						}
						else if (vAlign.get() == VerticalAlign.CENTER) {
							vAlign.set(VerticalAlign.TOP);
						}
						break;
					case LEFT:
						if (hAlign.get() == HorizontalAlign.RIGHT) {
							hAlign.set(HorizontalAlign.CENTER);
						}
						else if (hAlign.get() == HorizontalAlign.CENTER) {
							hAlign.set(HorizontalAlign.LEFT);
						}
						break;
					case RIGHT:
						Message<String> animStart = MessageBuilder
							.withPayload("")
							.setHeader(ShellMessageHeaderAccessor.EVENT_TYPE, EventLoop.Type.SYSTEM)
							.setHeader("animationstart", true)
							.build();
						component.getEventLoop().dispatch(animStart);
						// if (hAlign.get() == HorizontalAlign.LEFT) {
						// 	hAlign.set(HorizontalAlign.CENTER);
						// }
						// else if (hAlign.get() == HorizontalAlign.CENTER) {
						// 	hAlign.set(HorizontalAlign.RIGHT);
						// }
						break;
					default:
						break;
				}
			})
			.subscribe();

		// draw current date
		root.setDrawFunction((screen, rect) -> {
			int a = animX.get();
			Rectangle r = new View.Rectangle(rect.x() + 1 + a, rect.y() + 1, rect.width() - 2, rect.height() - 2);
			// Rectangle r = new View.Rectangle(rect.x() + 1, rect.y() + 1, rect.width() - 2, rect.height() - 2);
			String s = ref.get();
			if (s != null) {
				screen.printx(s, r, hAlign.get(), vAlign.get());
			}
			return rect;
		});

		// logic run
		component.setRoot(root, true);
		component.run();
	}

	/**
	 * Command handling snake game.
	 */
	@Command(command = "snakegame")
	public void snakegame() {

		// setup base game screen
		TerminalUI component = new TerminalUI(getTerminal());
		SnakeGame snakeGame = new SnakeGame(10, 10);
		BoxView root = new BoxView();
		root.setTitle("Snake");
		root.setShowBorder(true);

		AtomicInteger direction = new AtomicInteger();

		// handle arrow keys to game
		component.getEventLoop().keyEvents()
			.doOnNext(e -> {
				switch (e.key()) {
					case DOWN:
						direction.set(1);
						break;
					case UP:
						direction.set(-1);
						break;
					case LEFT:
						direction.set(-2);
						break;
					case RIGHT:
						direction.set(2);
						break;
					default:
						break;
				}
				snakeGame.update(direction.get());
			})
			.subscribe();

		// schedule game updates
		Disposable gameInterval = Flux.interval(Duration.ofMillis(500))
			.doOnNext(l -> {
				snakeGame.update(0);
				component.redraw();
			})
			.subscribe();
		// dispose when event loop is getting destroyd
		component.getEventLoop().onDestroy(gameInterval);

		// draw game area
		root.setDrawFunction((screen, rect) -> {
			snakeGame.draw(screen);
			return rect;
		});

		// fire up ui logic
		component.setRoot(root, true);
		component.run();
	}

	/**
	 * Classic snake game logic.
	 * 1. Snake starts in a center, initial direction needs arrow key
	 * 2. Arrows control snake direction
	 * 3. Eating a food crows a snake, new food is generated
	 * 4. Game ends if snake eats itself or goes out of bounds
	 * 5. Game ends if perfect score is established
	 */
	private static class SnakeGame {
		Board board;
		Game game;

		SnakeGame(int rows, int cols) {
			// snake starts from a center
			Cell initial = new Cell(rows / 2, cols / 2, 1);
			Snake snake = new Snake(initial);
			board = new Board(rows, cols, initial);
			game = new Game(snake, board);
		}

		void update(int direction) {
			if (direction != 0) {
				game.direction = direction;
			}
			game.update();
		}

		void draw(Screen screen) {
			Cell[][] cells = board.cells;
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[i].length; j++) {
					Cell cell = cells[i][j];
					String c = "";
					if (cell.type == 1) {
						c = "x";
					}
					else if (cell.type == -1) {
						c = "o";
					}
					screen.print(c, j + 1, i + 1, 1);
				}
			}
		}

		class Cell {
			final int row, col;
			// 0 - empty, > 0 - snake, < 0 - food
			int type;

			Cell(int row, int col, int type) {
				this.row = row;
				this.col = col;
				this.type = type;
			}
		}

		class Board {
			final int rows, cols;
			Cell[][] cells;

			Board(int rows, int cols, Cell initial) {
				this.rows = rows;
				this.cols = cols;
				cells = new Cell[rows][cols];
				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {
						cells[row][col] = new Cell(row, col, 0);
					}
				}
				cells[initial.row][initial.col] = initial;
				food();
			}

			void food() {
				int row = 0, column = 0;
				while (true) {
					row = (int) (Math.random() * rows);
					column = (int) (Math.random() * cols);
					if (cells[row][column].type != 1)
						break;
				}
				cells[row][column].type = -1;
			}
		}

		class Snake {
			LinkedList<Cell> cells = new LinkedList<>();
			Cell head;

			Snake(Cell cell) {
				head = cell;
				cells.add(head);
				head.type = 1;
			}

			void move(Cell cell, boolean grow) {
				if (!grow) {
					Cell tail = cells.removeLast();
					tail.type = 0;
				}
				head = cell;
				head.type = 1;
				cells.addFirst(head);
			}

			boolean checkCrash(Cell next) {
				for (Cell cell : cells) {
					log.info("Check cell {} {}", cell, next);
					if (cell == next) {
						return true;
					}
				}
				return false;
			}
		}

		class Game {
			Snake snake;
			Board board;
			int direction;
			boolean gameOver;

			Game(Snake snake, Board board) {
				this.snake = snake;
				this.board = board;
				this.direction = 0;
			}

			void update() {
				if (direction == 0) {
					return;
				}
				Cell next = next(snake.head);
				if (next == null || snake.checkCrash(next)) {
					direction = 0;
					gameOver = true;
				}
				else {
					boolean foundFood = next.type == -1;
					snake.move(next, foundFood);
					// log.info("Next cell type {}", next.type);
					if (foundFood) {
						board.food();
					}
				}
			}

			Cell next(Cell cell) {
				int row = cell.row;
				int col = cell.col;
				// return null if we're about to go out of bounds
				if (direction == 2) {
					col++;
					if (col >= board.cols) {
						return null;
					}
				}
				else if (direction == -2) {
					col--;
					if (col < 0) {
						return null;
					}
				}
				else if (direction == 1) {
					row++;
					if (row >= board.rows) {
						return null;
					}
				}
				else if (direction == -1) {
					row--;
					if (row < 0) {
						return null;
					}
				}
				return board.cells[row][col];
			}
		}
	}
}
