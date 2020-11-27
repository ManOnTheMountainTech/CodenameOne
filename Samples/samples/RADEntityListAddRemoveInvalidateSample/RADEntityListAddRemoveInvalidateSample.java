//require CodeRAD
package com.codename1.samples;


import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.ui.Toolbar;
import java.io.IOException;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.io.NetworkEvent;
import com.codename1.rad.controllers.ViewController;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityList;
import static com.codename1.rad.models.EntityTypeBuilder.entityTypeBuilder;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ListNode;
import com.codename1.rad.schemas.Thing;
import com.codename1.rad.ui.UI;
import com.codename1.rad.ui.entityviews.ProfileListView;
import com.codename1.ui.Button;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample was created to demonstrate using an internal collection to store rows in 
 * an EntityList.  The "Clear" button removes all items from the underlying list which
 * bypasses all of the usual change events in the EntityList, so after making the change
 * we need to call {@link EntityListView#invalidate()} to inform views that they need
 * to resynchronize with the list state.
 * 
 * <p>See <a href="https://github.com/shannah/CodeRAD">the CodeRAD github repo</a> for more
 * information about CodeRAD.</p>
 * 
 * <p>This sample was created for <a href="https://github.com/shannah/CodeRAD/issues/15">this issue</a>.</p>
 * 
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose 
 * of building native mobile applications using Java.
 */
public class RADEntityListAddRemoveInvalidateSample {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        Log.bindCrashProtection(true);

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            if(err.getError() != null) {
                Log.e(err.getError());
            }
            Log.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });        
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        
        // An entity for the rows our our list view.
        class Person extends Entity {}
        entityTypeBuilder(Person.class)
                .string(Thing.name)
                .factory(cls -> { return new Person(); })
                .build();
        
        // A remove row action that will be added to each row
        ActionNode removeRow = UI.action(UI.icon(FontImage.MATERIAL_DELETE));
        
        // An internal list we will use to store the rows of the entitylist
        ArrayList internalList = new ArrayList();
        EntityList profileList = new EntityList() {
            
            /**
             * Override createInternalList() so that we can use our own data structure
             * for storing this list.  This is contrived to allow us to test the 
             * invalidate() method.
             */
            @Override
            protected List createInternalList() {
                return internalList;
            }
            
        };
        
        // A list node to wrap our action and pass it to our view
        ListNode node = new ListNode(
                
                // Adding removeRow action to the ACCOUNT_LIST_ROW_ACTIONS category
                // causes the ProfileListView to render the action on each row 
                // of the list.
                UI.actions(ProfileListView.ACCOUNT_LIST_ROW_ACTIONS, removeRow)
        );
        
        // A ProfileListView to render the list. 
        // See https://shannah.github.io/CodeRAD/javadoc/com/codename1/rad/ui/entityviews/ProfileListView.html
        ProfileListView listView = new ProfileListView(profileList, node, 10);
        listView.setScrollableY(true);
        
        
        // Create a controller for the ProfileListView so that we can handle actions fired by the view.
        // Normally we'd do this in the FormController but since this sample doesn't have one
        // we create a ViewController for the ProfileListView directly.
        ViewController controller = new ViewController(null);
        controller.setView(listView);
        
        
        // Button to add rows to the list
        Button addRow = new Button(FontImage.MATERIAL_ADD);
        
        // Button to clear the list
        Button clear = new Button("Clear");
        
        
        addRow.addActionListener(evt-> {
            // "Add" button clicked. 
            // Create new person and add to the list
            Person p = new Person();
            p.set(Thing.name, "Row "+profileList.size());
            profileList.add(p);
            
            // This will trigger an EntityAddedEvent which will allow
            // the ProfileListView to synchronize
        });
        
        
        clear.addActionListener(evt->{
            // "Clear" button clicked
            
            // We could have called profileList.clear()
            // but this would send EntityRemoved events for each row removed 
            // which is inefficient.  Instead we'll clear the elements in
            // the internal list directly, and then call invalidate()
            // so that the ProfileListView knows to resynchronize its state.
            internalList.clear();
            profileList.invalidate();
        });
        
        
        
        controller.addActionListener(removeRow, evt -> {
            
            // The "Remove" button was clicked on a row.
            profileList.remove(evt.getEntity());
        });
        
        Form hi = new Form("Hi World", new BorderLayout());
        hi.add(NORTH, GridLayout.encloseIn(2, clear, addRow));
        hi.add(CENTER, listView);
        
        hi.show();
    }

    public void stop() {
        current = getCurrentForm();
        if(current instanceof Dialog) {
            ((Dialog)current).dispose();
            current = getCurrentForm();
        }
    }
    
    public void destroy() {
    }

}