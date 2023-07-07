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
package org.springframework.shell.samples.catalog.scenario.other;

import java.time.Duration;
import java.util.LinkedList;

import reactor.core.publisher.Flux;

import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.control.View;
import org.springframework.shell.component.view.event.KeyEvent.Key;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.samples.catalog.scenario.AbstractScenario;
import org.springframework.shell.samples.catalog.scenario.ScenarioComponent;

import static org.springframework.shell.samples.catalog.scenario.Scenario.CATEGORY_OTHER;

/**
 * Scenario implementing a classic snake game.
 *
 * Game logic.
 * 1. Snake starts in a center, initial direction needs arrow key
 * 2. Arrows control snake direction
 * 3. Eating a food crows a snake, new food is generated
 * 4. Game ends if snake eats itself or goes out of bounds
 * 5. Game ends if perfect score is established
 *
 * @author Janne Valkealahti
 */
@ScenarioComponent(name = "Snake", description = "Classic snake game", category = { CATEGORY_OTHER })
public class SnakeGameScenario extends AbstractScenario {

	@Override
	public View build() {
		SnakeGame snakeGame = new SnakeGame(10, 10);
		BoxView view = new BoxView();
		view.setTitle("Snake");
		view.setShowBorder(true);

		getEventloop().onDestroy(getEventloop().keyEvents()
			.subscribe(event -> {
				Integer direction = switch (event.key()) {
					case Key.CursorDown -> 1;
					case Key.CursorUp -> -1;
					case Key.CursorLeft -> -2;
					case Key.CursorRight -> 2;
					default -> 0;
				};
				if (direction != null) {
					snakeGame.update(direction);
				}
			}));

		// schedule game updates
		getEventloop().onDestroy(Flux.interval(Duration.ofMillis(500))
				.subscribe(l -> {
					snakeGame.update(0);
					getEventloop().dispatch(ShellMessageBuilder.ofRedraw());
				}
			));

		// draw game area
		view.setDrawFunction((screen, rect) -> {
			snakeGame.draw(screen);
			return rect;
		});
		return view;
	}

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
	}

	private static class Cell {
		final int row, col;
		// 0 - empty, > 0 - snake, < 0 - food
		int type;

		Cell(int row, int col, int type) {
			this.row = row;
			this.col = col;
			this.type = type;
		}
	}

	private static class Board {
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

	private static class Snake {
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
				// log.info("Check cell {} {}", cell, next);
				if (cell == next) {
					return true;
				}
			}
			return false;
		}
	}

	private static class Game {
		Snake snake;
		Board board;
		int direction;

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
