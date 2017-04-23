/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.saic.biclustering.movielens;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
/**
 *
 * @author jhyeh
 */
public class AnswerByLISA {
    private HashMap index;
    private String qafile;
    private double threshold;
    private int threadCount;
    private int port;
    private double degeneration;
    private int method;

    public static void main(String[] args) {
        double threshold = 0.75;
        try {
            threshold = Double.parseDouble(args[2]);
        } catch (Exception e) {
            threshold = 0.75;
        }
        int thread_count = 8;
        try {
            thread_count = Integer.parseInt(args[3]);
        } catch (Exception e) {
            thread_count = 8;
        }
        int m = 2;
        try {
            m = Integer.parseInt(args[4]);
        } catch (Exception e) {
            m = 2;
        }
        // necessary argument: sorpvoc.txt titles.map threshold, thread_count
        AnswerByLISA ablisa = new AnswerByLISA(args[0], args[1],
                threshold, thread_count, m);
        ablisa.go();
    }

    public AnswerByLISA(String indexFile, String qafile) {
        this(indexFile, qafile, 0.75, 8, 2, 8192);
    }

    public AnswerByLISA(String indexFile, String qafile, double th) {
        this(indexFile, qafile, th, 8, 2, 8192);
    }

    public AnswerByLISA(String indexFile, String qafile, double th, int tc) {
        this(indexFile, qafile, th, tc, 2, 8192);
    }

    public AnswerByLISA(String indexFile, String qafile, double th, int tc,
            int m) {
        this(indexFile, qafile, th, tc, m, 8192, 0.95);
    }

    public AnswerByLISA(String indexFile, String qafile, double th, int tc,
            int m, int p) {
        this(indexFile, qafile, th, tc, m, p, 0.95);
    }
    public AnswerByLISA(String indexFile, String qafile, double th, int tc,
            int m, int p, double deg) {
        this.qafile = qafile;
        this.threshold = th;
        this.threadCount = tc;
        this.port = p;
        this.method = m;
        this.degeneration = deg;
        // hash format: (file name: vs_[uid].txt) -> (topic vector: double[])
        try {
            ObjectInputStream ois = new ObjectInputStream(
                                       new FileInputStream(indexFile));
            this.index = (HashMap)ois.readObject();
            System.err.println("Topic vector index loaded, entry size = "+
                    index.keySet().size());
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList getQALines(BufferedReader br, int limit) throws IOException {
        int count=0;
        String line="";
        ArrayList result = new ArrayList();

        while ((line=br.readLine())!=null && count<limit) {
            result.add(line.trim());
            count++;
        }
        if (result.size() == 0) return null;
        return result;

    }

    public void go() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(qafile));

            while (true) {
                // fetch 512 lines as each batch
                ArrayList lines = getQALines(br, 512);
                if (lines == null) break;
                ExecutorService es = Executors.newFixedThreadPool(threadCount);
                for (Iterator it=lines.iterator(); it.hasNext(); ) {
                    String line = (String)it.next();
                    es.submit(new AnswerByLISAThread(line, index, threshold,
                            degeneration, method));
                }
                es.shutdown();
            }
            br.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void go() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(qafile));
            String line = "";

            AnswerByLISAThread[] th = new AnswerByLISAThread[this.threadCount];
            for (int i=0; i<this.threadCount; i++) th[i] = null;

            boolean done = false;
            while (!done) {
                for (int i=0; i<this.threadCount; i++) {
                    // find empty thread slot, Thread.isAlive() can't
                    // differentiate a new thread and a dead thread
                    if (th[i]==null || th[i].threadDone()) {
                        // get a question
                        if ((line=br.readLine()) != null) {
                            th[i] = new AnswerByLISAThread(line, this.index,
                                this.threshold);
                            th[i].start();
                        }
                        // no question, exit.
                        else done = true;
                    }
        		}
            }
            // to play safe: maybe the file reads out but threads are not stop
            for (int i=0; i<this.threadCount; i++) {
                if (th[i] != null) th[i].join();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*public void go() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(qafile));
            String line = "";
            int count = 0;
            //AnswerByLISAGrid[] th = new AnswerByLISAGrid[this.threadCount];
            AnswerByLISAThread[] th = new AnswerByLISAThread[this.threadCount];
            boolean done = false;
            while (!done) {
                if (count%100==0) System.err.println("Question "+count);
                //th[0] = th[1] = th[2] = th[3] = null;
                for (int i=0; i<this.threadCount; i++) {
                    th[i] = null;
                    if ((line=br.readLine()) != null) {
                        //th[i] = new AnswerByLISAGrid(line, this.index,
                        //        this.threshold, this.port, i);
                        th[i] = new AnswerByLISAThread(line, this.index,
                                this.threshold);
                        th[i].start();
                        count++;
                    }
                    else done = true;
        		}
                for (int i=0; i<this.threadCount; i++) {
        		    if (th[i] != null) th[i].join();
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    /*private String createQueryFile(String uid, String mid) {
        String fname = uid+"_4_"+mid+".txt";
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fname));
            pw.println(mid);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fname;
    }

    private double[] getDoubleValues(String[] slist) {
        double[] result = new double[slist.length];
        for (int i=0; i<slist.length; i++) {
            StringTokenizer st = new StringTokenizer(slist[i], ":");
            int pos = Integer.parseInt(st.nextToken());
            result[pos] = Double.parseDouble(st.nextToken());
        }
        return result;
    }*/


    /*private double calcAnswer(String uid, String mid) {
        // use mid as document content, run SORPQuery.search2
        String fname = this.createQueryFile(uid, mid);
        // to get the topic vector as topic weight parameters
        SORPQuery query = new SORPQuery(modelRoot, settings, vocabulary, "");
        query.setQueryFile(fname);
        String[] qresult = query.search2();
        try {
            // now we got [label]:[ratio] array
            // this is the topic vetor
            double[] dd = getDoubleValues(qresult);

            // check topics.log for user watched mids, find rate
            // get weight average with weight parameter and rates
        } catch (Exception e) {
            System.out.println("getDoubleValues error, file = "+fname);
        }
        return 0.0;
    }*/

}
