package id.co.elektroniksensus.pelepah;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.*;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.*;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private Uri cameraUri;
    private WebView printView;
    private static final int FILE_CHOOSER = 7001;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        webView=findViewById(R.id.webView);
        WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setMediaPlaybackRequiresUserGesture(false); s.setBuiltInZoomControls(false);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidApp");
        webView.setWebViewClient(new WebViewClient(){ @Override public void onPageStarted(WebView v,String u,Bitmap f){ super.onPageStarted(v,u,f); } });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params){
                if(fileCallback!=null) fileCallback.onReceiveValue(null); fileCallback=cb;
                Intent capture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File dir=new File(getCacheDir(),"camera"); dir.mkdirs(); File img=new File(dir,"photo_"+System.currentTimeMillis()+".jpg");
                cameraUri=FileProvider.getUriForFile(MainActivity.this,getPackageName()+".fileprovider",img); capture.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri); capture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent pick=new Intent(Intent.ACTION_GET_CONTENT); pick.addCategory(Intent.CATEGORY_OPENABLE); pick.setType("image/*");
                Intent chooser=new Intent(Intent.ACTION_CHOOSER); chooser.putExtra(Intent.EXTRA_INTENT,pick); chooser.putExtra(Intent.EXTRA_TITLE,"Ambil atau pilih foto"); chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,new Intent[]{capture}); startActivityForResult(chooser,FILE_CHOOSER); return true;
            }
        });
        webView.setDownloadListener((url,ua,cd,mime,len)->{
            if(url.startsWith("blob:")) webView.evaluateJavascript("(async()=>{const r=await fetch('"+url.replace("'","\\'")+"');const b=await r.blob();const q=new FileReader();q.onloadend=()=>AndroidApp.saveBase64(q.result,'"+guessName(cd,mime)+"');q.readAsDataURL(b)})()",null);
            else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
        webView.loadUrl("file:///android_asset/index.html");
    }
    private String guessName(String cd,String mime){ String n="dokumen_"+System.currentTimeMillis()+(mime!=null&&mime.contains("pdf")?".pdf":".bin"); if(cd!=null&&cd.contains("filename=")){ n=cd.substring(cd.indexOf("filename=")+9).replace("\"",""); } return n.replace("'","_"); }
    @Override protected void onActivityResult(int r,int c,@Nullable Intent d){ super.onActivityResult(r,c,d); if(r!=FILE_CHOOSER||fileCallback==null)return; Uri[] out=null; if(c==RESULT_OK){ if(d!=null&&d.getData()!=null)out=new Uri[]{d.getData()}; else if(cameraUri!=null)out=new Uri[]{cameraUri}; } fileCallback.onReceiveValue(out); fileCallback=null; }
    @Override public void onBackPressed(){ if(webView.canGoBack())webView.goBack(); else super.onBackPressed(); }
    public class AndroidBridge {
        @JavascriptInterface public void printHtml(final String html,final String title){
            runOnUiThread(()->{
                try{
                    final WebView w=new WebView(MainActivity.this); printView=w; final boolean[] done={false};
                    w.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String u){
                        if(done[0])return; done[0]=true;
                        PrintManager pm=(PrintManager)getSystemService(Context.PRINT_SERVICE);
                        PrintAttributes at=new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A3.asLandscape()).setMinMargins(new PrintAttributes.Margins(276,276,276,276)).build();
                        pm.print(title,v.createPrintDocumentAdapter(title),at);
                    }});
                    w.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
                }catch(Exception e){ Toast.makeText(MainActivity.this,"Gagal membuat PDF: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
            });
        }
        @JavascriptInterface public void saveBase64(String dataUrl,String requested){
            try { String raw=dataUrl.substring(dataUrl.indexOf(',')+1); byte[] bytes=Base64.decode(raw,Base64.DEFAULT); String name=(requested==null||requested.isEmpty()?"dokumen.pdf":requested); if(!name.toLowerCase().endsWith(".pdf"))name+=".pdf";
                if(Build.VERSION.SDK_INT>=29){ ContentValues v=new ContentValues(); v.put(MediaStore.Downloads.DISPLAY_NAME,name); v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf"); v.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/Elektronik Sensus Pelepah"); Uri u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v); try(OutputStream o=getContentResolver().openOutputStream(u)){o.write(bytes);} }
                else { File f=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),name); try(FileOutputStream o=new FileOutputStream(f)){o.write(bytes);} }
                runOnUiThread(()->Toast.makeText(MainActivity.this,"PDF tersimpan di folder Downloads",Toast.LENGTH_LONG).show());
            } catch(Exception e){ runOnUiThread(()->Toast.makeText(MainActivity.this,"Gagal menyimpan PDF: "+e.getMessage(),Toast.LENGTH_LONG).show()); }
        }
    }
}
