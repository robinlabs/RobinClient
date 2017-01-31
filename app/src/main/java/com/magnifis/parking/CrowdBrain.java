package com.magnifis.parking;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oded on 12/31/13.
 */
public class CrowdBrain {

    private static void newFreeFormIntent(Context context, String command, String intent, final ChangeListener changeListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(contextTheme(context));
        LinearLayout root = getDialogRootLayout(context, command, intent, changeListener);
        alert.setView(root);
        alert.show();
    }

    private static void newIntentDialog(Context context, ViewGroup viewGroup) {
        AlertDialog.Builder alert = new AlertDialog.Builder(contextTheme(context));
        alert.setView(viewGroup);
        alert.show();
    }

    static void shortcutMenu(Context context, final String failedCommand, final ChangeListener changeListener) {
        context = contextTheme(context);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        Button synonymButton = new Button(context);
        Button openAppButton = new Button(context);
        Button searchAppButton = new Button(context);
        Button shareTextButton = new Button(context);
        Button advancedButton = new Button(context);
        final EditText editText = new EditText(context);
        if (failedCommand != null) {
            editText.setText(failedCommand);
            editText.setTextSize(getTextSize());
            editText.setHint("Command");
            layout.addView(editText);
        }

        List<Button> buttons = new ArrayList<Button>(Arrays.asList(new Button[]{synonymButton, openAppButton, searchAppButton, shareTextButton, advancedButton}));

        synonymButton.setText("Add synonym");
        openAppButton.setText("Open an app");
        searchAppButton.setText("Search inside an app");
        shareTextButton.setText("Share text to an app");
        advancedButton.setText("Advanced");

        synonymButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(android.R.drawable.ic_menu_edit), null, null, null);
        openAppButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(android.R.drawable.ic_menu_set_as), null, null, null);
        searchAppButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(android.R.drawable.ic_menu_search), null, null, null);
        shareTextButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(android.R.drawable.ic_menu_share), null, null, null);
        advancedButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(android.R.drawable.ic_menu_preferences), null, null, null);


        final Context finalContext = context;
        synonymButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newIntentDialog(finalContext, getAddSynonymRootLayout(finalContext, editText.getText().toString(), changeListener));
            }
        });
        openAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newIntentDialog(finalContext, getOpenAppIntentComposerRootLayout(finalContext, editText.getText().toString(), changeListener));
            }
        });
        searchAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newIntentDialog(finalContext, getSearchIntentComposerRootLayout(finalContext, editText.getText().toString(), changeListener));
            }
        });
        shareTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newIntentDialog(finalContext, getSendTextIntentComposerRootLayout(finalContext, editText.getText().toString(), changeListener));
            }
        });
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFreeFormIntent(finalContext, editText.getText().toString(), null, changeListener);
            }
        });


        //do for all

        for (Button b : buttons) {
            b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            b.setCompoundDrawablePadding(8);
            layout.addView(b);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(contextTheme(context));
        builder.setView(layout);
        builder.create().show();
    }

    private static void oldShortcutMenu(final Context context, final String failedCommand, final ChangeListener changeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(contextTheme(context));
        builder.setTitle("What type of action would you like to teach me?");
        builder.setItems(new CharSequence[]{"Add synonym", "Open an app", "Search inside an app", "Share text to an app", "Advanced"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                newIntentDialog(context, getAddSynonymRootLayout(context, failedCommand, changeListener));
                                break;
                            case 1:
                                newIntentDialog(context, getOpenAppIntentComposerRootLayout(context, failedCommand, changeListener));
                                break;
                            case 2:
                                newIntentDialog(context, getSearchIntentComposerRootLayout(context, failedCommand, changeListener));
                                break;
                            case 3:
                                newIntentDialog(context, getSendTextIntentComposerRootLayout(context, failedCommand, changeListener));
                                break;
                            case 4:
                                newFreeFormIntent(context, failedCommand, null, changeListener);
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private static LinearLayout getOpenAppIntentComposerRootLayout(Context ctx, String failedCommand, final ChangeListener changeListener) {
        final Context context = contextTheme(ctx);

        LinearLayout linearLayout = new LinearLayout(context);
        final EditText command = new EditText(context);
        final Spinner apps = new Spinner(context);
        Button save = new Button(context);
//
//        Intent intent = new Intent(Intent.ACTION_MAIN, null);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        apps.setAdapter(new InstalledAppsAdapter(context, null));

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        command.setHint("Alias");
        if (failedCommand != null) {
            command.setText(failedCommand);
        }
        command.setTextSize(getTextSize());

        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationInfo ri = (ApplicationInfo) apps.getSelectedItem();
                String pName = ri.packageName;
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pName);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                saveIntent(context, command.getText().toString(), launchIntent.toUri(0), changeListener);
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        linearLayout.addView(command);
        linearLayout.addView(apps);
        linearLayout.addView(save);
        linearLayout.setPadding(32, 32, 32, 32);

        return linearLayout;
    }

    private static LinearLayout getSearchIntentComposerRootLayout(Context ctx, String failedCommand, final ChangeListener changeListener) {
        final Context context = contextTheme(ctx);

        LinearLayout linearLayout = new LinearLayout(context);
        final EditText command = new EditText(context);
        final Spinner apps = new Spinner(context);
        final EditText query = new EditText(context);
        Button save = new Button(context);

        final Intent sampleIntent = new Intent(Intent.ACTION_SEARCH);

        apps.setAdapter(new InstalledAppsAdapter(context, sampleIntent));

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        command.setHint("Command");
        if (failedCommand != null) {
            command.setText(failedCommand);
        }
        command.setTextSize(getTextSize());
        query.setHint("Search term");
        query.setTextSize(getTextSize());


        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent();
                searchIntent.setAction(Intent.ACTION_SEARCH);
                ApplicationInfo ri = (ApplicationInfo) apps.getSelectedItem();
                String pName = ri.packageName;
                searchIntent.setPackage(pName);
                searchIntent.putExtra(SearchManager.QUERY, query.getText().toString());
                searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                saveIntent(context, command.getText().toString(), searchIntent.toUri(0), changeListener);
//                context.startActivity(searchIntent);
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        linearLayout.addView(command);
        linearLayout.addView(apps);
        linearLayout.addView(query);
        linearLayout.addView(save);
        linearLayout.setPadding(32, 32, 32, 32);

        return linearLayout;
    }

    private static LinearLayout getSendTextIntentComposerRootLayout(Context ctx, String failedCommand, final ChangeListener changeListener) {
        final Context context = contextTheme(ctx);

        LinearLayout linearLayout = new LinearLayout(context);
        final EditText command = new EditText(context);
        final Spinner apps = new Spinner(context);
        final EditText query = new EditText(context);
        Button save = new Button(context);

        final Intent sampleIntent = new Intent(Intent.ACTION_SEND);
        sampleIntent.setType("text/plain");


        apps.setAdapter(new InstalledAppsAdapter(context, sampleIntent));

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        command.setHint("Command");
        if (failedCommand != null) {
            command.setText(failedCommand);
        }
        command.setTextSize(getTextSize());
        query.setHint("Text to send");
        query.setTextSize(getTextSize());


        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, query.getText().toString());
                sendIntent.setType("text/plain");
                ApplicationInfo ri = (ApplicationInfo) apps.getSelectedItem();
                String pName = ri.packageName;
                sendIntent.setPackage(pName);
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                saveIntent(context, command.getText().toString(), sendIntent.toUri(0), changeListener);
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        linearLayout.addView(command);
        linearLayout.addView(apps);
        linearLayout.addView(query);
        linearLayout.addView(save);
        linearLayout.setPadding(32, 32, 32, 32);

        return linearLayout;
    }

    private static float getTextSize() {
        return 20;
    }

    private static LinearLayout getDialogRootLayout(Context context, String commandString, String intentString, final ChangeListener changeListener) {

        context = contextTheme(context);

        LinearLayout linearLayout = new LinearLayout(context);
        EditText command = new EditText(context);
        EditText intent = new EditText(context);
        Button save = new Button(context);
        TextView examples = new TextView(context);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        command.setHint("text to trigger the intent");
        intent.setHint("intent URI that performs the action");

        if (commandString != null) command.setText(commandString);
        if (intentString != null) intent.setText(intentString);

        save.setText("Save");
        save.setOnClickListener(new SaveIntentClickListener(context, command, intent, changeListener));
        examples.setText("  examples...");
        examples.setTextColor(Color.parseColor("#0099cc"));
        examples.setOnClickListener(new ExamplesDialog(context, changeListener));
        linearLayout.addView(command);
        linearLayout.addView(intent);
        linearLayout.addView(save);
        linearLayout.addView(examples);
        linearLayout.setPadding(32, 32, 32, 32);

        return linearLayout;
    }

    private static LinearLayout getAddSynonymRootLayout(Context context, String synonymString, final ChangeListener changeListener) {

        context = contextTheme(context);

        LinearLayout linearLayout = new LinearLayout(context);
        EditText synonym = new EditText(context);
        EditText command = new EditText(context);
        Button save = new Button(context);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        synonym.setHint("Synonym");
        command.setHint("Which command should this trigger");

        if (synonymString != null) synonym.setText(synonymString);

        save.setText("Save");
        save.setOnClickListener(new SaveIntentClickListener(context, synonym, command, changeListener));

        linearLayout.addView(synonym);
        linearLayout.addView(command);
        linearLayout.addView(save);
        linearLayout.setPadding(32, 32, 32, 32);

        return linearLayout;
    }

    private static Context contextTheme(Context context) {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
        }
        return context;
    }

    public static Map<String, String> getAllIntents(Context context) {
        return (Map<String, String>) sp(context).getAll();
    }

    private static SharedPreferences sp(Context context) {
        return context.getSharedPreferences("CROWD_BRAIN_SHARED_PREFS", Context.MODE_PRIVATE);
    }

    public static void saveIntent(Context ctx, String command, String intent, final ChangeListener changeListener) {
        sp(ctx).edit().putString(command, intent).commit();
        if (changeListener != null) {
            changeListener.onNewIntentAdded(new HashMap.SimpleEntry<String,String>(command,intent));
        }
    }

    public static void deleteIntent(Context ctx, String key) {
        sp(ctx).edit().remove(key).commit();
    }

    public interface ChangeListener {

        public void onNewIntentAdded(Map.Entry<String,String> entry);
    }

    private static class ExamplesDialog implements View.OnClickListener {
        Context context;
        ChangeListener changeListener;

        public ExamplesDialog(Context context, final ChangeListener changeListener) {
            this.context = context;
            this.changeListener = changeListener;
        }

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(contextTheme(context));
            builder.setTitle("Sample intents");
            final String[] commands = new String[]{"show calc", "Tell my girlfriend I love her", "play cat videos on youtube"};
            builder.setItems(commands,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent calcIntent = new Intent();
                            calcIntent.setAction(Intent.ACTION_MAIN);
                            calcIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            calcIntent.setPackage("com.android.calculator2");
//                            calcIntent.setComponent(new ComponentName("com.android.calculator2", "com.android.calculator2.Calculator"));

                            Intent loveIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:1234567890"));
                            loveIntent.putExtra("sms_body", "I LOVE YOU");

                            Intent vidIntent = new Intent(Intent.ACTION_SEARCH);
                            vidIntent.setPackage("com.google.android.youtube");
                            vidIntent.putExtra("query", "cats");

                            switch (which) {
                                case 0:
                                    newFreeFormIntent(context, commands[which], calcIntent.toUri(0), changeListener);
                                    break;
                                case 1:
                                    newFreeFormIntent(context, commands[which], loveIntent.toUri(0), changeListener);
                                    break;
                                case 2:
                                    newFreeFormIntent(context, commands[which], vidIntent.toUri(0), changeListener);
                                    break;
                            }
                        }
                    });
            builder.create().show();

        }
    }

    private static class SaveIntentClickListener implements View.OnClickListener {


        private final EditText command;
        private final EditText intent;
        Context ctx;
        ChangeListener changeListener;

        public SaveIntentClickListener(Context ctx, EditText command, EditText intent, final ChangeListener changeListener) {
            this.command = command;
            this.intent = intent;
            this.ctx = ctx;
            this.changeListener = changeListener;
        }

        @Override
        public void onClick(View v) {
            saveIntent(ctx, command.getText().toString(), intent.getText().toString(), changeListener);
            Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show();
        }


    }

    /*static class CandidateApp {
        int icon;
        String packageName;
        String appName;
    }*/

    private static class InstalledAppsAdapter extends BaseAdapter implements SpinnerAdapter {

        //        List<ResolveInfo> resolvedInfoList;
        List<ApplicationInfo> applicationInfoList = new ArrayList<ApplicationInfo>();
        PackageManager pm;

        InstalledAppsAdapter(Context context, Intent sampleIntent) {
            pm = context.getPackageManager();

            if (sampleIntent != null) {
                List<ResolveInfo> resolvedInfoList = pm.queryIntentActivities(sampleIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo ri : resolvedInfoList) {
                    ApplicationInfo ai = ri.activityInfo.applicationInfo;
                    applicationInfoList.add(ai);
                }
            } else {
                List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo ai : applications) {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(ai.packageName);
                    if (launchIntent != null) {
                        applicationInfoList.add(ai);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return applicationInfoList.size();
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return applicationInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                Context context = parent.getContext();
                holder.linearLayout = new LinearLayout(context);
                holder.linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                convertView = holder.linearLayout;

                holder.iconView = new ImageView(context);
                holder.nameView = new TextView(context);
                holder.linearLayout.addView(holder.iconView);
                holder.linearLayout.addView(holder.nameView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ApplicationInfo app = applicationInfoList.get(position);
            holder.iconView.setImageDrawable(app.loadIcon(pm));

            holder.nameView.setText(app.loadLabel(pm));
            holder.nameView.setTextSize(getTextSize());


            return convertView;
        }
    }

    static class ViewHolder {
        LinearLayout linearLayout;
        ImageView iconView;
        TextView nameView;
    }
}
