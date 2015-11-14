package fr.xtof54.detsport;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetSport extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button leq = (Button) findViewById(R.id.leq);
        leq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leq21();
            }
        });

        final Button mcs = (Button) findViewById(R.id.mcs);
        mcs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcs();
            }
        });
    }

    private void print(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.text);
                t.append(s);
            }
        });
    }

    private void mcs() {
        final String mcsurl = "http://www.machainesport.fr/guide-tv";
        print("MCS");

        Connection c = new Connection(mcsurl, new Connection.Handler() {
            @Override
            public void gotResult(final String res) {
                // nb de jours affiches
                int nsousblocs = 0;
                {
                    int k=res.indexOf("daysRow");
                    Pattern p = Pattern.compile("<div|</div");
                    Matcher m = p.matcher(res.substring(k));
                    int inblock = 1;
                    while (m.find()) {
                        if (res.charAt(k+m.start() + 1) == '/') {
                            inblock--;
                            if (inblock == 0) break;
                        } else {
                            nsousblocs++;
                            inblock++;
                        }
                    }
                }
                final int njours = nsousblocs;
                print("\n"+"njours "+njours+"\n");
                StringBuilder[] perday = new StringBuilder[njours];
                for (int i=0;i<njours;i++) perday[i]=new StringBuilder();

                for (int i=0;;) {
                    int j=res.indexOf("\"sectionRow ",i);
                    if (j<0) break;
                    for (int z=0;z<njours;z++) {
                        int k = res.indexOf("\"calendarcell\"", j);
                        if (k<0) break;
                        Pattern p = Pattern.compile("<div|</div");
                        Matcher m = p.matcher(res.substring(k));
                        int inblock = 1;
                        while (m.find()) {
                            if (res.charAt(k+m.start() + 1) == '/') {
                                inblock--;
                                if (inblock == 0) break;
                            } else {
                                inblock++;
                                if (inblock == 2) {
                                    int a=res.indexOf("calendarStartTime",k+m.start());
                                    if (a>=0) {
                                        perday[z].append(res.substring(a+19,a+24));
                                        perday[z].append(" ");
                                        int b=res.indexOf("calendarProgTitle",a);
                                        if (b>=0) {
                                            int c=res.indexOf('<',b);
                                            String xx = res.substring(b+19,c).replace('\n',' ');
                                            perday[z].append(xx);
                                        }
                                        perday[z].append("\n");
                                    }
                                }
                            }
                        }
                        j=k+1;
                    }
                    i=j;
                }
                print(perday[0].toString());
                for (int i=1;i<perday.length;i++) {
                    print("\n...jour suivant...\n");
                    print(perday[i].toString());
                }
            }
        });
        c.connect();
    }

    private void leq21() {
        final String leq21url = "http://www.lequipe21.fr/grille";
        print("L'équipe 21");

        Connection c = new Connection(leq21url, new Connection.Handler() {
            @Override
            public void gotResult(final String res) {
                final Pattern rexp = Pattern.compile("<div>\\d\\d:\\d\\d");
                final Matcher m = rexp.matcher(res);
                StringBuilder sb = new StringBuilder();
                for (; ; ) {
                    if (!m.find()) break;
                    int deb = m.start();
                    int fin = m.end();
                    sb.append(res.substring(deb+5, fin));
                    sb.append(" ");
                    int i=res.indexOf("<h3>",fin);
                    if (i>=0) {
                        int j=res.indexOf("</h3>",i);
                        if (j>=0) sb.append(res.substring(i+4,j));
                    }
                    sb.append("\n");
                }
                print(sb.toString());
            }
        });
        c.connect();
    }
}
