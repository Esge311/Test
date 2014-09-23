package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity 
	extends ActionBarActivity
	implements TextToSpeech.OnInitListener
{x

	private static final int MY_DATA_CHECK_CODE = 13761;
	private TextView mTextView;
	private TextView mNfcTag;
	private NfcAdapter mNfcAdapter;
	private TextToSpeech mTts;
	private ArrayList<String> mTextToSpeak = new ArrayList<String>(); 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.description);
        mNfcTag = (TextView) findViewById(R.id.nfcTag);
        
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter == null) 
        {
        	Toast.makeText(this, "This device doesn't support NFC", Toast.LENGTH_LONG).show();
        	this.finish();
        	return;
        }
      
        if(mNfcAdapter.isEnabled() == false)
        {
        	mTextView.setText("NFC is disabled");
        	this.speakOut("NFC disabled.");
        }
        else
        {
        	mTextView.setText("NFC enabled");
        }
        
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        
        handleIntent(this.getIntent());
    }

    private void handleIntent(Intent intent)
    {
    	if(intent.getAction() == NfcAdapter.ACTION_TECH_DISCOVERED)
    	{
    		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    		byte[] rawTagId = tag.getId(); 
    		String tagId = Common.bytesToHex(rawTagId);
   		
    		mNfcTag.setText("NFC Tag: " + tagId);
    		
    		this.speakOut("NFC Tag: " + tagId);
    	}
    }

    private void speakOut() 
    {
    	this.speakOut(null);
    }
    
    private void speakOut(String textToSpeak) 
    {
		if(mTts != null)
    	{
	    	if(mTextToSpeak.size() > 0)
	    	{
	    		for(String text : mTextToSpeak)
	    		{
	    			mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
	    		}
	    		
	    		mTextToSpeak.clear();
	    	}
    	
	    	if(textToSpeak != null)
    		{
    			mTts.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null);
    		}
    	}
		else
		{
			mTextToSpeak.add(textToSpeak);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) 
        {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if (requestCode == MY_DATA_CHECK_CODE) 
        {        
        	if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) 
        	{            
        		// success, create the TTS instance            
        		mTts = new TextToSpeech(this, this);        
        	} 
        	else 
        	{            
        		// missing data, install it            
        		Intent installIntent = new Intent();            
        		installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);            
        		startActivity(installIntent);        
        	}
        }
        
    	super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	public void onInit(int status) 
	{
		 if (status == TextToSpeech.SUCCESS) 
		 {
			 int result = mTts.setLanguage(Locale.US);
	 
	         if (result == TextToSpeech.LANG_MISSING_DATA || 
	        		 result == TextToSpeech.LANG_NOT_SUPPORTED) 
	         {
	            Log.e("TTS", "This Language is not supported");
	         } 
	         else 
	         {
	        	 Log.i("TTS", "Succesfully initialized");
	        	 this.speakOut();
             }
	     } 
		 else 
		 {
			 Log.e("TTS", "Initilization Failed!");
	     }
	}
}
