package org.wildstang.wildrank.androidv2.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.wildstang.wildrank.androidv2.R;
import org.wildstang.wildrank.androidv2.Utilities;
import org.wildstang.wildrank.androidv2.activities.ScoutMatchActivity;
import org.wildstang.wildrank.androidv2.adapters.MatchListAdapter;
import org.wildstang.wildrank.androidv2.data.DatabaseManager;
import org.wildstang.wildrank.androidv2.views.TemplatedTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mail929 on 4/3/15.
 */
public class ScoutersFragment  extends Fragment
{

    private ListView list;
    private TextView red1;
    private TextView red2;
    private TextView red3;
    private TextView blue1;
    private TextView blue2;
    private TextView blue3;
    private TextView red1Scouter;
    private TextView red2Scouter;
    private TextView red3Scouter;
    private TextView blue1Scouter;
    private TextView blue2Scouter;
    private TextView blue3Scouter;
    private TextView matchNum;

    public ScoutersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scouters, container, false);

        List<MatchDataContainer> matches = new ArrayList<>();
        try {
            List<DataContainer> data = runQuery();
            for(int i = 0; i < data.size(); i++)
            {
                DataContainer dataPoint = data.get(i);
                boolean foundMatch = false;
                for(int j = 0; j < matches.size(); j++)
                {
                    MatchDataContainer match = matches.get(j);
                    if(match.match.equals(dataPoint.match))
                    {
                        foundMatch = true;
                        match.teams.add(dataPoint.team);
                        for(int l = 0; l < dataPoint.scouters.size(); l++)
                        {
                            match.scouters.add(dataPoint.scouters.get(l));
                        }
                        System.out.println("Adding " + dataPoint.team + " to " + dataPoint.match);
                    }
                }
                if(!foundMatch)
                {
                    System.out.println("Creating " + dataPoint.match + " with " + dataPoint.team);
                    matches.add(new MatchDataContainer(dataPoint.team, dataPoint.match, dataPoint.scouters.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        list = (ListView) view.findViewById(R.id.listView);
        list.setAdapter(new ArrayAdapter<MatchDataContainer>(getActivity(), R.layout.list_item_scouters, R.id.red1, matches) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView == null) {
                    LayoutInflater infl = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.list_item_scouters, parent, false);
                }
                view = super.getView(position, convertView, parent);

                MatchDataContainer match = matches.get(position);
                matchNum = (TextView) view.findViewById(R.id.match_number);
                matchNum.setText(match.match);
                if(match.scouters.size() >= 1)
                {
                    red1 = (TextView) view.findViewById(R.id.red1);
                    red1.setText(match.teams.get(0) + " - ");
                    red1Scouter = (TextView) view.findViewById(R.id.red1Scouter);
                    red1Scouter.setText(match.scouters.get(0));
                }

                if(match.scouters.size() >= 2)
                {
                    red2 = (TextView) view.findViewById(R.id.red2);
                    red2.setText(match.teams.get(1) + " - ");
                    red2Scouter = (TextView) view.findViewById(R.id.red2Scouter);
                    red2Scouter.setText(match.scouters.get(1));
                }

                if(match.scouters.size() >= 3)
                {
                    red3 = (TextView) view.findViewById(R.id.red3);
                    red3.setText(match.teams.get(2) + " - ");
                    red3Scouter = (TextView) view.findViewById(R.id.red3Scouter);
                    red3Scouter.setText(match.scouters.get(2));
                }

                if(match.scouters.size() >= 4)
                {
                    blue1 = (TextView) view.findViewById(R.id.blue1);
                    blue1.setText(match.teams.get(3) + " - ");
                    blue1Scouter = (TextView) view.findViewById(R.id.blue1Scouter);
                    blue1Scouter.setText(match.scouters.get(3));
                }

                if(match.scouters.size() >= 5)
                {
                    blue2 = (TextView) view.findViewById(R.id.blue2);
                    blue2.setText(match.teams.get(4) + " - ");
                    blue2Scouter = (TextView) view.findViewById(R.id.blue2Scouter);
                    blue2Scouter.setText(match.scouters.get(4));
                }

                if(match.scouters.size() >= 6)
                {
                    blue3 = (TextView) view.findViewById(R.id.blue3);
                    blue3.setText(match.teams.get(5) + " - ");
                    blue3Scouter = (TextView) view.findViewById(R.id.blue3Scouter);
                    blue3Scouter.setText(match.scouters.get(5));
                }
                return view;
            }
        });
        return view;
    }

    private List<DataContainer> runQuery() throws Exception {
        Query query = DatabaseManager.getInstance(getActivity()).getAllCompleteMatches();

        List<DataContainer> data = new ArrayList<>();
        QueryEnumerator enumerator = query.run();

        List<QueryRow> queryRows = new ArrayList<>();
        int counter = 0;
        for (Iterator<QueryRow> it = enumerator; it.hasNext(); ) {
            QueryRow row = it.next();
            String team = Utilities.teamNumberFromTeamKey(row.getDocument().getProperty("team_key").toString());
            String match = String.valueOf(Utilities.matchNumberFromMatchKey(row.getDocument().getProperty("match_key").toString()));
            List<String> ids = (ArrayList<String>) row.getDocument().getProperty("users");
            List<String> scouters = new ArrayList<>();
            for(int i = 0; i < ids.size(); i++)
            {
                scouters.add(DatabaseManager.getInstance(getActivity()).getUserById(ids.get(i)).getProperty("name").toString());
            }
            data.add(new DataContainer(team, match, scouters));
            counter++;
            //System.out.println("Row " + counter + " is match " + match + " for team " + team);
            queryRows.add(row);
        }
        return data;
    }

    public class DataContainer
    {
        String team;
        String match;
        List<String> scouters;

        public DataContainer(String team, String match, List<String> scouters)
        {
            this.team = team;
            this.match = match;
            this.scouters = scouters;
            //System.out.println("New data container, containing " + match + " for " + team);
        }
    }

    public class MatchDataContainer
    {
        List<String> teams = new ArrayList<>();
        String match;
        List<String> scouters = new ArrayList<>();

        public MatchDataContainer(String team, String match, String scouter)
        {
            teams.add(team);
            this.match = match;
            scouters.add(scouter);
        }

        public void add(String team, String scouter) {
            teams.add(team);
            scouters.add(scouter);
        }
    }
}