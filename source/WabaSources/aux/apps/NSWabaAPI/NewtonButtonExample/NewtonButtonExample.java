import waba.ui.*;
import newton.*;


/**
<b>	NewtonButtonExample	</b>

<p>The purpose of this example is to demonstrate the basics in creating
a callback function in Waba.  NewtonScript uses callback functions
for all sorts of stuff -- but Java doesn't have any notion of closures
or automatically-built objects, beyond certain inner class stuff.

<p>The way a callback function is created is through subclassing 
the Callback class.  You create a method which takes an array of
Refs (the arguments passed in by NewtonScript) and returns a Ref
(the return value to give back to NewtonScript).  Then you have the
Callback build a NewtonScript callback function of N arguments, out
of your method, and register it with a mailbox of callbacks.

<p>The example also shows how to splat a button on the screen and also
how easy it is to have this stuff coexist with Waba UI elements (the Label
for example).

<p>-- Sean
<p>To compile:
<p><pre>

javac NewtonButtonExample.java
java wababin.Warp c /f 4 NewtonButtonExample
java wababin.Exegen /h 40 /w 100 /f 4 NewtonButtonExample NewtonButtonExample NewtonButtonExample
open -a PackType NewtonButtonExample.pkg

</pre>
<p>Then drag the NewtonButtonExample.pkg file to PackType, which will give it
type signature necessary to drag it to NCU.  Then use NCU to download it to your Newton.

<p> -- Sean
*/


 public class NewtonButtonExample extends MainWindow
    {
    public void onStart()
        {
        // We start by making an ordinary Waba label
        Label label = new Label("Press:");
        label.setRect(10, 10, 80, 30);
        add(label);
        
        // Now we will make and install a real NewtonScript button
        
        // make a frame which points to the button proto
        Ref buttontemplate = NS.frameWithProto("protoTextButton");
        // set its viewbounds
        buttontemplate.setSlot("viewBounds",NS.bounds(40,10,80,30));
        // set its text value
        buttontemplate.setSlot("text",new Ref("Wow!"));
        
        // make a callback to get called when the button is pressed
        Callback mycallback = new Callback()
            {
            public Ref call(Ref[] args)
                {
                // let's pop up a notify window when the button presses us!
                Ref.call("GetRoot").send("Notify",
                    new Ref(4 /*kNotifyQAlert*/),
                    new Ref("Waba for the Newton!!!"),
                    new Ref("Isn't this fun?"));
                // return nil
                return new Ref();
                // This method is where you also often remove yourself
                // from the Callback Mailbox if you're one-shot.  But
                // we don't need to remove this callback from the mailbox
                // because it's not one-shot -- it'll get called by NS over
                // and over again, every time the button is pressed, and
                // only would need to be removed when the application quits
                }
            };
        
        // 
        // Our callback function is supposed to take 0 arguments
        // in this example.
        Ref myfunc = mycallback.func(0);
        
        // Set this callback function as the button's ButtonClickScript
        buttontemplate.setSlot("ButtonClickScript", myfunc);
        
        // add the button as a stepview of the Waba drawing area view
        Ref button = Ref.call("AddStepView", NS.wabaDrawingArea(), buttontemplate);
        
        // Because AddStepView asked us to (though it's not necessary in this
        // situation) let's have good style and set the button to be dirty.
        button.send("Dirty");
        }
    }




