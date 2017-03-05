

package rogue_opcode;


import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * Interface Saveable allows instances (ScreenElements usually) that need to be
 * saved between Android lifecycle transitions to be written to persistent
 * storage.
 *
 * Below find a complete RogueOp application that implements the Saveable
 * interface.
 * <pre>
 * package com.tooleyc.saveable_test;
 *
 *
 * import com.tooleyc.saveable_test.R;
 * import com.tooleyc.saveable_test.Saveable.StateManager;
 *
 * import rogue_opcode.AnimatedView;
 * import rogue_opcode.GameProc;
 * import rogue_opcode.GraphicResource;
 * import rogue_opcode.ScreenElement;
 * import rogue_opcode.geometrics.XYZf;
 *
 *
 * public class Saveable_test extends GameProc
 * {
 * static final int BASE_WIDTH = 320;
 * static final int BASE_HEIGHT = 480;
 *
 * @Override
 *           public void InitializeOnce()
 *           {
 *           AnimatedView.sOnly.NormailzeResolution(BASE_WIDTH, BASE_HEIGHT);
 *           AnimatedView.sOnly.Debug(true);
 *           }
 *
 *           public void Shutdown()
 *           {
 *           StateManager.Save();
 *           }
 * @Override
 *           public void InitializeOnResume()
 *           {
 *           if (!StateManager.Load()) {
 *           //hmm, couldn't find any state, need to make some
 *           StateManager.Add(new SaveableScreenElement(R.drawable.bug, 50));
 *           StateManager.Add(new SaveableScreenElement(R.drawable.bug, 100));
 *           StateManager.Add(new SaveableScreenElement(R.drawable.bug, 150));
 *           }
 *           }
 *           }
 *
 *
 *           class SaveableScreenElement extends ScreenElement implements
 *           Saveable
 *           {
 *           private static final long serialVersionUID = 1L;
 *
 *           int mTestVal;
 *
 *           public SaveableScreenElement()
 *           {
 *           super(0);
 *           mTestVal = 100;
 *           }
 *
 *           public SaveableScreenElement(int pResourceID, int pXPos)
 *           {
 *           super(pResourceID);
 *           mTestVal = 200;
 *
 *           mPos.x = pXPos;
 *           mPos.y = 250;
 *           }
 *
 *           public String BuildStateString()
 *           {
 *           return SaveableScreenElement.class.getCanonicalName() + " " +
 *           "mPos.x="
 *           + mPos.x + " mPos.y=" + mPos.y;
 *           }
 *
 *           public void LoadState(StateManager pStateManager)
 *           {
 *           mPos.x = pStateManager.NextFloat();
 *           mPos.y = pStateManager.NextFloat();
 *
 *           mGR = GraphicResource.FindGR(R.drawable.bug);
 *           }
 *
 *           public void Update()
 *           {
 *           XYZf tXY = new XYZf();
 *           tXY.x = mPos.x;
 *           tXY.y = mPos.y;
 *
 *           if(GameProc.sOnly.Touching())
 *           {
 *           mPos.y -= .1f;
 *           }
 *
 *           }
 *           }
 *           </pre>
 *
 * @author Christopher R. Tooley
 * @see StateManager
 */
public interface Saveable
{
	static Set<Saveable> mAllSaveables = new HashSet<Saveable>();

	/**
	 * LoadState will be called by the static StateManager.Load() method. The
	 * StateManager holds a string of state information that contains the
	 * parameters your object deemed necessary to preserve state at the time
	 * BuildStateString was called.
	 *
	 * @param pStateManager is an instance of StateManager that is created for
	 *        this object. Use pStateManager to initialize member variables
	 *        using the data parsing functions.
	 */
	public void LoadState(StateManager pStateManager);

	/**
	 * BuildStateString needs to return a String containing all data necessary
	 * to restore state.
	 * A single non-terminated space-delimited line starting with the
	 * implementing classe's canonical name and followed by "name=value" pairs.
	 * Example: com.tooleyc.saveable.SaveableScreenElement mPos.x=100 mPos.y=200
	 *
	 * @return the text serialization of the object.
	 */
	public String BuildStateString();

	/**
	 * StateManager holds instance state string information and contains
	 * functions for parsing out this information into individual parameters.
	 *
	 * @author Christopher R. Tooley
	 * @author Brigham Toskin
	 *
	 */
	public class StateManager
	{
		String[] mState;
		int mParamIndex;

		/**
		 * Save() is a static method that should be called by your application
		 * in
		 * the Shutdown() method. The Save() method calls the BuildStateString()
		 * method on all of the instances that have implemented the Saveable
		 * interface and have been registered via the Add() method. The
		 * individual state strings are concatenated and saved to
		 * persistent storage.
		 */
		//TODO - this should be called from GameProc automagically for us
		public static void Save()
		{

			String tStateString = "";
			for(Saveable tSaveable : mAllSaveables)
			{
				tStateString += tSaveable.BuildStateString() + "\n";
			}

			//TODO GameState should be RogueOP
			SharedPreferences tPrefs = GameProc.sOnly.getSharedPreferences(
					"GameState1", Context.MODE_PRIVATE);
			Editor tPrefsEditor = tPrefs.edit();
			tPrefsEditor.putString("SavedState", tStateString);
			tPrefsEditor.commit();
		}

		/**
		 * Load() is called from your InitializeOnResume method and is
		 * responsible for locating the persisted state information. If this
		 * information is found new instances of these objects will be created
		 * and initialized to their former state. If the state information is
		 * not found a value of false will be returned - in this case your
		 * application is responsible for creating all instances and adding them
		 * to the StateManager via the Add() method.
		 *
		 * @return true if a previously saved state was found and loaded;
		 *         otherwise false.
		 */
		public static boolean Load()
		{
			SharedPreferences tPrefs = GameProc.sOnly.getSharedPreferences(
					"GameState1", Context.MODE_PRIVATE);

			String tAllStates = tPrefs.getString("SavedState", "");

			if (tAllStates.length() == 0)
				return false;

			String[] tStates = tAllStates.split("\n");

			for(String tState : tStates)
			{
				StateManager.newInstance(tState);
			}

			return true;
		}

		public static boolean SavedStateExists() {
			SharedPreferences tPrefs = GameProc.sOnly.getSharedPreferences(
					"GameState1", Context.MODE_PRIVATE);

			String tAllStates = tPrefs.getString("SavedState", "");

			if (tAllStates.length() == 0)
				return false;

			return true;
		}

		/**
		 * Clear() is a static method that can be called to erase the persisted
		 * state information from the shared storage.
		 */
		public static void Clear()
		{
			SharedPreferences tPrefs = GameProc.sOnly.getSharedPreferences(
					"GameState1", Context.MODE_PRIVATE);
			Editor tPrefsEditor = tPrefs.edit();
			tPrefsEditor.remove("SavedState");
			tPrefsEditor.commit();
		}

		//Called internally by Load() to create a new instance of a class from persisted parameter data
		@SuppressWarnings("rawtypes")
		private static Object newInstance(String pSavedStateString)
		{
			if(pSavedStateString == null)
				return null;

			try
			{
				if(pSavedStateString.length() != 0)
				{
					Class tClass = Class
							.forName(pSavedStateString.split(" ")[0]);
					Object tClassInstance = tClass.newInstance();
					Class[] tClasses = tClass.getInterfaces();
					for(Class tSubclass : tClasses)
						if(tSubclass.equals(Saveable.class))
						{

							((Saveable)tClassInstance)
									.LoadState(new StateManager(
											(Saveable)tClassInstance,
											pSavedStateString));
							break;
						}
					return tClassInstance;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}

		//Called internally by newInstance() to build and return a StateManager for a newly created object
		private StateManager(Saveable pSaveable, String pState)
		{
			mAllSaveables.add(pSaveable);
			mState = pState.split(" ");
			mParamIndex = 1;
		}

		/**
		 * Add() is used to add a Saveable to the Set of managed objects
		 *
		 * @param pSaveable is the Saveable instance to be managed
		 */
		public static void Add(Saveable pSaveable)
		{
			mAllSaveables.add(pSaveable);
		}

		/**
		 * NextInt() is used by your implementation of the LoadState() method to
		 * retrieve the next parameter as an integer value.
		 *
		 * @return the parsed Integer value of the current parameter in the
		 *         StateManager
		 */
		public int NextInt()
		{
			try
			{
				return Integer.parseInt(mState[mParamIndex++].split("=")[1]);
			}
			catch(Exception e)
			{
				return 0;
			}
		}

		/**
		 * NextFloat() is used by your implementation of the LoadState() method
		 * to
		 * retrieve the next parameter as a Float value.
		 *
		 * @return the parsed Float value of the current parameter in the
		 *         StateManager
		 */
		public float NextFloat()
		{
			try
			{
				return Float.parseFloat(mState[mParamIndex++].split("=")[1]);
			}
			catch(Exception e)
			{
				return 0;
			}
		}

		/**
		 * NextString() is used by your implementation of the LoadState() method
		 * to
		 * retrieve the next parameter as a String value.
		 *
		 * @return the String value of the current parameter in the
		 *         StateManager
		 */
		public String NextString()
		{
			try
			{
				return mState[mParamIndex++].split("=")[1]; //FIXME - this only supports single word "strings"!
			}
			catch(Exception e)
			{
				return "";
			}
		}

	}
}
