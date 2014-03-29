package com.group23.towerdefense;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ResourceManager
{
	private static final String TAG = "ResourceManager";

	public static Texture loadTexture(String filename)
	{
		return load(filename, sTextureMap, textureLoader);
	}

	public static Music loadMusic(String filename)
	{
		return load(filename, sMusicMap, musicLoader);
	}
	
	public static BitmapFont loadFont(String filename)
	{
		return load(filename, sFontMap, fontLoader);
	}

	public static void dispose()
	{
		disposeMap(sTextureMap);
		disposeMap(sMusicMap);
		disposeMap(sFontMap);
	}

	private static ArrayMap<String, Texture> sTextureMap = new ArrayMap<String, Texture>();
	private static ArrayMap<String, Music> sMusicMap = new ArrayMap<String, Music>();
	private static ArrayMap<String, BitmapFont> sFontMap = new ArrayMap<String, BitmapFont>();

	private interface Loader<Resource>
	{
		Resource load(String filename);
	}

	private static Loader<Texture> textureLoader = new Loader<Texture>()
	{
		@Override
		public Texture load(String filename)
		{
			return new Texture(Gdx.files.internal(filename));
		}
	};

	private static Loader<Music> musicLoader = new Loader<Music>()
	{
		@Override
		public Music load(String filename)
		{
			return Gdx.audio.newMusic(Gdx.files.internal(filename));
		}
	};

	private static Loader<BitmapFont> fontLoader = new Loader<BitmapFont>()
	{
		@Override
		public BitmapFont load(String filename)
		{
			return new BitmapFont(Gdx.files.internal(filename));
		}
	};

	private static <Resource> Resource load(String filename,
			ArrayMap<String, Resource> map, Loader<Resource> loader)
	{
		try
		{
			Resource res = map.get(filename);
			if (res == null)
			{
				res = loader.load(filename);
				map.put(filename, res);
			}
			return res;
		}
		catch (GdxRuntimeException e)
		{
			Gdx.app.log(TAG, e.getMessage());
			return null;
		}
	}

	private static <Resource extends Disposable> void disposeMap(
			ArrayMap<String, Resource> map)
	{
		ArrayMap.Values<Resource> resources = map.values();
		for (Resource resource : resources)
			resource.dispose();
		map.clear();
	}

	private ResourceManager()
	{
		// Class is non-instantiable
	}
}
