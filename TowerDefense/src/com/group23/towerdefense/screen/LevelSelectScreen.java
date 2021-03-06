package com.group23.towerdefense.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.group23.towerdefense.DefaultLevelGenerator;
import com.group23.towerdefense.Level;
import com.group23.towerdefense.ResourceManager;
import com.group23.towerdefense.TowerDefense;
import com.group23.towerdefense.ui.ImageButton;

public class LevelSelectScreen extends BaseScreen
{
	private Level.Generator generator = new DefaultLevelGenerator();
	private Actor[] LevelButtons = new Actor[5];
	private Actor[] diffButtons = new Actor[3];
	

	@Override
	public void show()
	{
		super.show();
		
		Stage stage = getStage();

		Image background = new Image(
				ResourceManager.loadTexture("levelmenu.png"));
		stage.addActor(background);

		if (TowerDefense.maxLevel != 0)
		{
			for (int i = 0; i < 5; i++)
			{
				String imageFilename = String.format("level%d_b.png", i + 1);
				Actor levelSelectButton = new LevelSelectButton(imageFilename,
						i);
				levelSelectButton.setBounds(800.0f, (5 - i) * 100.0f + 50.0f,
						200.0f, 60.0f);
				levelSelectButton.setVisible(i <= TowerDefense.maxLevel);

				stage.addActor(levelSelectButton);
				LevelButtons[i] = levelSelectButton;
			}
		}
		else
		{
			Actor startButton = new NewGameButton();
			stage.addActor(startButton);
		}

		
		for(int i = 0; i <= 2; i++)
		{
			String imageFilename = "";
			switch(i)
			{
				case 0: 
					imageFilename = "easy.png";
					break;
				case 1:
					imageFilename = "medium.png";
					break;
				case 2: 
					imageFilename = "hard.png";
					break;
			}
			Actor difficultySelectButton = new difficultySelectButton(imageFilename, i);
			difficultySelectButton.setBounds(550.0f + 250f * i,  50.0f,
					200.0f, 60.0f);
			stage.addActor(difficultySelectButton);
			difficultySelectButton.setVisible(true);
			diffButtons[i] = difficultySelectButton;
		}
	};
	
	/**
	 * Actor representing the level select buttons on the level select screen.
	 * When pressed, they will load the appropriate level for the user.
	 * 
	 * @author Jacob
	 *
	 */

	private class LevelSelectButton extends ImageButton
	{
		private int levelNum;

		public LevelSelectButton(String imageFilename, int level)
		{
			super(imageFilename);
			this.levelNum = level;
		}

		@Override
		protected void onPressed()
		{
			TowerDefense.curLevel = levelNum;
			Level level = generator.getLevel(levelNum);
			TowerDefense.changeScreen(new GameplayScreen(level));
		}
	}
	
	/**
	 * Actor representing the difficulty select buttons on the level select
	 * screen. When pressed, they will set the gameplay difficulty to the
	 * respective level.
	 * 
	 * @author Cole
	 *
	 */
	
	private class difficultySelectButton extends ImageButton
	{
		private int difficulty;
		
		public difficultySelectButton(String imageFilename, int difficulty) 
		{
			super(imageFilename);
			this.difficulty = difficulty;
		}

		protected void onPressed() 
		{
			TowerDefense.difficulty = difficulty;
		}
		
	}
	
	/**
	 * Actor representing the Begin button on the level select screen whenever
	 * the user has no save files on record. When pressed, it will load level 1.
	 * 
	 * @author Jacob
	 *
	 */

	private class NewGameButton extends LevelSelectButton
	{
		public NewGameButton()
		{
			super("begin_b.png", 0);
			setBounds(660.0f, 420.0f, 512.0f, 256.0f);
		}		
		protected void onPressed()
		{
			
			Level level = generator.getLevel(0);
			TowerDefense.changeScreen(new GameplayScreen(level));
		}
	}
}
