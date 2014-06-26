package com.noeuli.widgetproviderlist;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class WidgetProviderListActivity extends Activity {
    private static final String TAG = "WidgetProviderListActivity";
    
    private Context mContext;
    private ListView mListView;
    private WidgetProviderListAdapter mListViewAdapter;
    private ArrayList<AppWidgetProviderInfo> mWidgetProviderList;
    private EditText mDetails;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_widget_provider_list);
        setupData();
        setupViews();
    }
    
    private void setupData() {
        mWidgetProviderList = (ArrayList<AppWidgetProviderInfo>) getAllProviders();
        printAllWidgetProviders(mWidgetProviderList);
        mListViewAdapter = new WidgetProviderListAdapter(mWidgetProviderList);
    }
    
    private void setupViews() {
        mListView = (ListView) findViewById(R.id.listView);
        if (mListView != null) {
            mListView.setAdapter(mListViewAdapter);
            //mListView.setOnClickListener(mClickListener);
            mListView.setOnItemClickListener(mItemClickListener);
        }
        mDetails = (EditText) findViewById(R.id.txtDetails);
    }
    
    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            AppWidgetProviderInfo info = mWidgetProviderList.get(position);
            String desc = getWidgetInfo(position, info);
            mDetails.setText(desc);
            Log.d(TAG, "onItemClick(" + position + ") " + desc);
        }
    };

    @SuppressLint("InlinedApi")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<AppWidgetProviderInfo> getAllProviders() {
        AppWidgetManager awm = AppWidgetManager.getInstance(mContext);
        if (awm == null) {
            Log.e(TAG, "get AppWidgetManager failed while getAllProviders()");
            return new ArrayList<AppWidgetProviderInfo>();
        }

        String methodName ="getInstalledProvidersAll";
        Class[] paramTypes = new Class[] { Integer.TYPE };
        Object[] params = new Object[] { Integer.valueOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN) };
        List<AppWidgetProviderInfo> widgetProviders = null;

        String fallbackMethod = "getInstalledProviders";
        
        if (Build.VERSION.SDK_INT < 17) {
            paramTypes = null;
            params = null;
        }
        widgetProviders = (List<AppWidgetProviderInfo>) Util.invoke(awm, methodName, paramTypes, params,
                fallbackMethod, null, null);
        
        if (widgetProviders == null) {
            Log.e(TAG, "getAllProviders(): Failed on getting installed widget providers list.");
            widgetProviders = new ArrayList<AppWidgetProviderInfo>();
        }

        return widgetProviders;
    }

    private class WidgetProviderListAdapter extends BaseAdapter {
        private static final String TAG = "WidgetProviderListAdapter";
        
        private ArrayList<AppWidgetProviderInfo> mWidgetProviderList;
        
        public WidgetProviderListAdapter(ArrayList<AppWidgetProviderInfo> list) {
            mWidgetProviderList = list;
        }
        
        @Override
        public int getCount() {
            return mWidgetProviderList != null ? mWidgetProviderList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            if (mWidgetProviderList!=null && mWidgetProviderList.size() > position) {
                return mWidgetProviderList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (mWidgetProviderList!=null && mWidgetProviderList.size() > position) {
                return position;
            }
            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            AppWidgetProviderInfo info = null;
            
            if (mWidgetProviderList == null || mWidgetProviderList.size() <= position) {
                // Error.
                Log.e(TAG, "getView(" + position + ") Can't get the item in that position!");
                return null;
            }
            
            info = mWidgetProviderList.get(position);
            
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
//                convertView.setId(position);
//                convertView.setOnClickListener(mClickListener);
                holder.textView = (TextView) convertView.findViewById(R.id.list_item_Text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String desc = getWidgetInfo(position, info);
            holder.textView.setText(desc);
            //Log.d(TAG, "getView(" + position + ") " + desc);
            
            return convertView;
        }
        
        private class ViewHolder {
            TextView textView;
        }
    }

    private String getWidgetInfo(int index, AppWidgetProviderInfo info) {
        return getWidgetInfo(index, info, true);
    }
    
    @SuppressLint("NewApi")
    private String getWidgetInfo(int index, AppWidgetProviderInfo info, boolean line) {
        StringBuilder desc = new StringBuilder("[" + index + "] ");
        desc.append(info.label);
        if (info.provider != null) {
            desc.append(" ");
            desc.append(info.provider.getClassName());
        }
        if (line) desc.append("\n");
        else desc.append(" ");
        desc.append(info.minWidth);
        desc.append("x");
        desc.append(info.minHeight);
        if (info.resizeMode > 0) {
            if (info.resizeMode == 1) {
                desc.append(" resize- ");
            } else if (info.resizeMode == 2) {
                desc.append(" resize| ");
            } else if (info.resizeMode == 3) {
                desc.append(" resize+ ");
            }
            desc.append(info.minResizeWidth);
            desc.append("x");
            desc.append(info.minResizeHeight);
        }
        if (info.updatePeriodMillis > 0) {
            desc.append(" ");
            desc.append(info.updatePeriodMillis/1000);
            desc.append("s");
        }
        if (Build.VERSION.SDK_INT > 16) {
            if (info.widgetCategory==1) {
                desc.append(" home");
            } else if (info.widgetCategory==2) {
                desc.append(" lock");
            } else if (info.widgetCategory==3){
                desc.append(" both");
            }
        }
        if (info.configure!=null) {
            if (line) desc.append("\n");
            else desc.append(" ");
            desc.append(info.configure.getClassName());
        }
        
        return desc.toString();
    }
    private void printAllWidgetProviders(ArrayList<AppWidgetProviderInfo> list) {
        Log.i(TAG, "printAllWidgets. size=" + list.size() + " " + Build.MODEL + " API " + Build.VERSION.SDK_INT);
        for (int i=0; i<list.size(); i++) {
            Log.d(TAG, getWidgetInfo(i, list.get(i), false));
        }
    }
    
    private static class Util {
        /**
         * Find given method name and call it. If method does not exist, return null.
         *
         * @param receiver The object on which to call this method.
         * @param methodName The requested method's name.
         * @param paramTypes The parameter types of the requested method. "(Class[])null" is equivalent to the empty array.
         * @param paramList The arguements to the method.
         * @return The result. If failed to call the method or the method's return type is void, then returns null.
         * 
         * @see #invoke(Object, String, Class[], Object[], String, Class[], Object[])
         *
         * @author P11873 Bongho Cho
         * @version 1.0
         */
        @SuppressWarnings({ "rawtypes", "unused" })
        public static Object invoke(Object receiver, String methodName, Class[] paramTypes, Object[] paramList) {
            return invoke(receiver, methodName, paramTypes, paramList, null, null, null);
        }
        
        /**
         * Find given method name and call it. If method does not exist, try again with given fallback method.
         * If calling fallback method failed, then return null.
         *
         * @param receiver The object on which to call this method.
         * @param methodName The requested method's name.
         * @param paramTypes The parameter types of the requested method. "(Class[])null" is equivalent to the empty array.
         * @param paramList The arguements to the method.
         * @param fallbackMethod The requested fallback method's name.
         * @param fallbackParamTypes The parameter types of the requested fallback method.
         * @param fallbackParams The arguements to the fallback method.
         * @return The result. If failed to call method or the method's return type is void, then returns null.
         * 
         * @see #invoke(Object, String, Class[], Object[])
         *
         * @author P11873 Bongho Cho
         * @version 1.0
         */
        @SuppressWarnings("rawtypes")
        public static Object invoke(Object receiver, String methodName, Class[] paramTypes, Object[] paramList, 
                String fallbackMethod, Class[] fallbackParamTypes, Object[] fallbackParams) {

            // Check arguements
            if (receiver==null || methodName==null) {
                Log.e(TAG, "invoke error: parameter null!");
                return null;
            }

            Class<?> klass = receiver.getClass();
            boolean notFound = false;
            Method method = null;
            Object[] params = null;

            // Try to call given method.
            try {
                method = klass.getMethod(methodName, paramTypes);
                if (method != null) params = paramList;
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "invoke error[1]: No such method " + methodName + " in object " + receiver.getClass().getName(), e);
                notFound = true;
            }

            // Method not found. Try with fallback method.
            if (notFound && fallbackMethod!=null) {
                try {
                    method = klass.getMethod(fallbackMethod, fallbackParamTypes);
                    if (method!=null) params = fallbackParams;
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "invoke error[2]: No fallback method " + fallbackMethod + " in object " + receiver.getClass().getName(), e);
                    return null;
                }
            }

            // If method found, invoke that.
            try {
                if (method !=null) {
                    // If given method has no return type, then just call the method and return null.
                    if (method.getReturnType().getName().equals("void")) {
                        method.invoke(receiver, params);
                        return null;
                    } else {
                        return method.invoke(receiver, params);
                    }
                } else {
                    Log.e(TAG, "invoke error: method not found!");
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "invoke error:", e);
                return null;
            }
        }        
    }
}
