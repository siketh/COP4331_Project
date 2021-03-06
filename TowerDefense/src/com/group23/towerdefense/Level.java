package com.group23.towerdefense;

import java.util.Arrays;

import com.badlogic.gdx.utils.Array;
import com.group23.towerdefense.enemy.Enemy;
import com.group23.towerdefense.spawn.LevelWave;
import com.group23.towerdefense.spawn.Wave;
import com.group23.towerdefense.tower.Tower;

public class Level
{
	/**
	 * 
	 * @author Robert
	 * 
	 */
	public static class Builder
	{
		private int[] tiles;
		private LevelWave waves = new LevelWave();
		private int startGold = 500, startLives = 10;

		public Level build()
		{
			return new Level(this);
		}

		public Builder setTiles(int[] tiles)
		{
			this.tiles = tiles;
			return this;
		}

		public Builder addWave(Wave.Generator generator)
		{
			waves.addWave(generator);
			return this;
		}

		public Builder setStartGold(int gold)
		{
			this.startGold = gold;
			return this;
		}

		public Builder setStartLives(int lives)
		{
			this.startLives = lives;
			return this;
		}
	}

	public static abstract class Generator
	{
		public abstract Level getLevel(int levelNum);
	}

	// Level constants
	public final static int NUM_TILES_WIDTH = 15;
	public final static int NUM_TILES_HEIGHT = 8;
	private final static int MAX_TOWERS = 10;
	public final static int TILE_WIDTH = 128;
	public final static int TILE_HEIGHT = 128;

	// Tile data
	private int[] tiles;
	private Dir[] directions;

	// Player Statistics
	int playerGold;
	int playerLives;
	int levelNum;

	private Array<Tower> towers;

	private Array<Enemy> enemies;
	private LevelWave wave;

	/**
	 * Initializes the level class For now, the level uses a pre-defined tile
	 * array and direction array
	 */
	private Level(Builder builder)
	{
		/**
		 * Retrieve variables from builder
		 */

		tiles = builder.tiles;
		wave = builder.waves;
		playerGold = builder.startGold;
		playerLives = builder.startLives;

		/**
		 * Initialize internal classes
		 */

		enemies = new Array<Enemy>();
		towers = new Array<Tower>();
		directions = new Dir[tiles.length];

		/**
		 * Create the direction map based on the tiles
		 */
		createDirMap();
	}

	// Creates a direction map for enemies to follow, based off of the tile map
	private void createDirMap()
	{
		Arrays.fill(directions, Dir.I); // Initialize all cells to invalid

		boolean leftEdge; // True if current index is on the left edge of the
							// map
		boolean rightEdge; // True if current index is on the right edge of the
							// map
		boolean topEdge; // True if current index is on the top edge of the map
		boolean bottomEdge; // True if current index is on the bottom edge of
							// the map

		// DFS loop, start at whichever index the starting cell is
		// Sets the direction FROM the current cell TO the next cell
		// Runs as long as we aren't on a base cell
		int i = getStart();
		while (tiles[i] != 2)
		{
			// Initialize edge cases to false
			leftEdge = false;
			rightEdge = false;
			topEdge = false;
			bottomEdge = false;

			// Determine if we are currently on an edge
			if (i % getWidth() == 0)
				leftEdge = true;
			if (i % getWidth() == getWidth() - 1)
				rightEdge = true;
			if (i / getWidth() == 0)
				topEdge = true;
			if (i / getWidth() == getHeight() - 1)
				bottomEdge = true;

			// If we are not on the top edge, the northern tile is not 0, and we
			// have not
			// already set the direction of the northern tile, set current
			// tile's direction
			// to north and make it our new pathfinding index.
			// Repeat similar process for all directions.
			if (topEdge == false
					&& (tiles[i - 15] != 0)
					&& directions[i - 15] == Dir.I)
			{
				directions[i] = Dir.N;
				i = i - 15;
			}
			// East is 1 tile forward
			else if (rightEdge == false
					&& (tiles[i + 1] != 0)
					&& directions[i + 1] == Dir.I)
			{
				directions[i] = Dir.E;
				i = i + 1;
			}
			// South is 15 tiles forward
			else if (bottomEdge == false
					&& (tiles[i + 15] != 0)
					&& directions[i + 15] == Dir.I)
			{
				directions[i] = Dir.S;
				i = i + 15;
			}
			// West is 1 tile backwards
			else if (leftEdge == false
					&& (tiles[i - 1] != 0)
					&& directions[i - 1] == Dir.I)
			{
				directions[i] = Dir.W;
				i = i - 1;
			}
			// NE is 14 tiles backwards
			else if (topEdge == false
					&& rightEdge == false
					&& (tiles[i - 14] != 0)
					&& directions[i - 14] == Dir.I)
			{
				directions[i] = Dir.NE;
				i = i - 14;
			}
			// SE is 16 tiles forwards
			else if (bottomEdge == false
					&& rightEdge == false
					&& (tiles[i + 16] != 0)
					&& directions[i + 16] == Dir.I)
			{
				directions[i] = Dir.SE;
				i = i + 16;
			}
			// SW is 14 tiles forwards
			else if (bottomEdge == false
					&& leftEdge == false
					&& (tiles[i + 14] != 0)
					&& directions[i + 14] == Dir.I)
			{
				directions[i] = Dir.SW;
				i = i + 14;
			}
			// NW is 16 tiles backwards
			else if (topEdge == false
					&& leftEdge == false
					&& (tiles[i - 16] != 0)
					&& directions[i - 16] == Dir.I)
			{
				directions[i] = Dir.NW;
				i = i - 16;
			}
			else
				break;
		}
		// Loop exited since we are on a base tile, so make this the end tile
		directions[i] = Dir.End;
	}

	/**
	 * Updates the positions and values of all the enemies, towers and wave
	 * spawning system.
	 * 
	 * @param dt
	 *            The amount of time since the last update
	 */
	public void act(float dt)
	{
		wave.act(this);

		// Update enemies
		for (Enemy e : enemies)
			e.act(dt);

		// Update towers
		for (Tower t : towers)
			t.act();
	}

	/**
	 * Places a tower on a tile-coordinate position if: <li>The tile is empty</li>
	 * <li>No other tower is already placed</li> <br/>
	 * WARNING: No check is done to ensure boundary
	 * 
	 * @param x
	 *            Tile x-coordinate to place the new tower
	 * @param y
	 *            Tile y-coordinate to place the new tower
	 */
	public void placeTower(Tower tower, int x, int y)
	{
		if (canPlaceTower(x, y) && tower != null)
		{
			if (tower.getGoldCost() <= playerGold)
			{
				towers.add(tower);
				tower.registerToLevel(this, x, y);
				playerGold -= tower.getGoldCost();
				getTower(x, y);
			}
		}
	}

	/**
	 * Removes a tower on a tile-coordinate position
	 * 
	 * @param tower
	 * @return true if tower removed
	 */
	public boolean removeTower(Tower tower)
	{
		return towers.removeValue(tower, false);
	}

	/**
	 * Selects a tower on a tile-coordinate position
	 * 
	 * @param x
	 *            Tile x-coordinate to select tower
	 * @param y
	 *            Tile y-coordinate to select tower
	 */
	public Tower getTower(int x, int y)
	{
		final int index = convertTileToIndex(x, y);
		for (Tower t : towers)
			if (t.getTile() == index)
				return t;
		return null;
	}

	/**
	 * Checks if a tower can be placed on a tile coordinate position
	 * 
	 * @param x
	 *            Tile x-coordinate to check
	 * @param y
	 *            Tile y-coordinate to check
	 * @return True if a tower can be positioned
	 */
	private boolean canPlaceTower(int x, int y)
	{
		// Check if tile is an empty tile
		if (getTile(x, y) != 0)
			return false;
		// Check if tile is a tower bar tile
		else if (x >= 4 && x < 9 && y == 0)
			return false;
		// Check that no towers conflict with position
		final int pos = y * getWidth() + x;
		for (Tower t : towers)
			if (t.getTile() == pos)
				return false;

		return towers.size < MAX_TOWERS;
	}

	/**
	 * Return the direction of the start of the path
	 * 
	 * @return The direction of the start of the path
	 */
	public Dir getStartDir()
	{
		return directions[getStart()];
	}

	/**
	 * Return the tile index of the start of the path
	 * 
	 * @return The tile index of the start of the path
	 */
	public int getStart()
	{
		int i = 0;
		while (tiles[i] != 3)
			i++;
		return i;
	}

	/**
	 * Return the tile index of the end of the path
	 * 
	 * @return The tile index of the end of the path
	 */
	public int getEnd()
	{
		int i = 0;
		while (tiles[i] != 2)
			i++;
		return i;
	}

	/**
	 * Returns the number of tiles wide the tilemap is
	 * 
	 * @return The width of the tilemap
	 */
	public int getWidth()
	{
		return NUM_TILES_WIDTH;
	}

	/**
	 * Returns the number of tiles high the tilemap is
	 * 
	 * @return The height of the tilemap
	 */
	public int getHeight()
	{
		return NUM_TILES_HEIGHT;
	}

	/**
	 * Returns the tile value of a specific tile
	 * 
	 * @param tile
	 *            The tile you want the value of
	 * @return The tile value
	 */
	public int getTile(int tile)
	{
		return tiles[tile];
	}

	/**
	 * Returns the tile value of a specific tile
	 * 
	 * @param x
	 *            The column of the tile
	 * @param y
	 *            The row of the tile
	 * @return The tile value
	 */
	public int getTile(int x, int y)
	{
		return getTile(y * getWidth() + x);
	}

	/**
	 * Returns the Direction of the tile index
	 * 
	 * @param tile
	 *            Index of the tile to find the direction for
	 * @return Direction of the tile
	 */
	public Dir getDirection(int tile)
	{
		return directions[tile];
	}

	// Get direction in tile x and y coordinates
	public Dir getDirection(int x, int y)
	{
		return getDirection(y * getWidth() + x);
	}

	/**
	 * Returns the Array of Enemies
	 * 
	 * @return The Enemy Arrays
	 */
	public Array<Enemy> getEnemies()
	{
		return enemies;
	}

	/**
	 * Removes an enemy from the enemy array
	 * 
	 * @param enemy
	 *            The enemy to be Removed
	 */
	public void removeEnemy(Enemy enemy)
	{
		enemy.setAlive(false);
		enemies.removeValue(enemy, false);
	}

	/**
	 * Removes an enemy from the enemy array and subtracts their lives from the
	 * players lives
	 * 
	 * @param enemy
	 *            The enemy to be Removed
	 */
	public void enemyReachedEnd(Enemy enemy)
	{
		playerLives -= enemy.getLives();
		enemies.removeValue(enemy, false);
	}

	/**
	 * Converts a tile coordinate to index coordinate
	 * 
	 * @param x
	 *            Tile x-coordinate
	 * @param y
	 *            Tile y-coordinate
	 * @return index position of the tile (x,y) coordinate
	 */
	private int convertTileToIndex(int x, int y)
	{
		return y * getWidth() + x;
	}

	/**
	 * Fixes tiles so they show exactly as they are rendered in the code
	 * 
	 * @param tiles
	 * @return Fixed version of tiles
	 */
	@SuppressWarnings("unused")
	private int[] fixTiles(int[] tiles)
	{
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight() / 2; y++)
			{
				int posA = y * getWidth() + x;
				int posB = (getHeight() - y) * getWidth() - (getWidth() - x);

				System.out.printf("posA:%d;posB:%d\n", posA, posB);

				int swap = tiles[posA];
				tiles[posA] = tiles[posB];
				tiles[posB] = swap;
			}

		return tiles;
	}

	/**
	 * Adds gold to the player
	 * 
	 * @param goldValue
	 *            The amount of gold to be added
	 */
	public void giveGold(int goldValue)
	{
		playerGold += goldValue;
	}

	/**
	 * Returns the amount of gold the player has
	 * 
	 * @return The amount of gold the player has
	 */
	public int getGold()
	{
		return playerGold;
	}
	
	public void subtractGold(int gold)
	{
		playerGold -= gold;
	}

	/**
	 * Returns the amount of lives the player has
	 * 
	 * @return The amount of lives the player has
	 */
	public int getLives()
	{
		return playerLives;
	}

	public Array<Tower> getTowers()
	{
		return towers;
	}

	public boolean isWavePlaying()
	{
		return wave.isPlaying();
	}

	public boolean hasFinishedAllWaves()
	{
		return wave.isFinished();
	}

	public void startNextWave()
	{
		wave.next(enemies);
	}
}
