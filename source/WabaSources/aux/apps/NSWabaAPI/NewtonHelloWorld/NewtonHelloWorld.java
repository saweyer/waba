import waba.ui.*;
import newton.*;

/*
NewtonHelloWorld

The purpose of this example is to demonstrate the basics in accessing
NewtonScript to draw in Waba.  What we're going to do is create a
label (a protoStaticText object), set it to say "Hello, World!", and
add it as a subview of our Waba view.  Note the use of various
NS helper methods and the use of Refs and methods on them.

-- Sean
*/

/*
To compile:

javac NewtonHelloWorld.java
java wababin.Warp c /f 4 NewtonHelloWorld
java wababin.Exegen /h 40 /w 100 /f 4 NewtonHelloWorld NewtonHelloWorld NewtonHelloWorld
open -a PackType NewtonHelloWorld.pkg

Drag the NewtonHelloWorld.pkg file to PackType, which will give it
type signature necessary to drag it to NCU.  Then use NCU to download it to your Newton.
*/


public class NewtonHelloWorld extends MainWindow
    {
    public void onStart()
        {
        // first we make a frame that points to the protoStaticText proto
        // in its _proto slot.
        Ref labeltemplate = NS.frameWithProto("protoStaticText");
        
        // set its viewBounds slot
        labeltemplate.setSlot("viewBounds", NS.bounds(10,10,100,30));
        
        // set its text value
        labeltemplate.setSlot("text", new Ref("Hello, World!"));
        
        // Add the template as a stepview of to the wabaDrawingArea
        // view.  This will return us a completed view frame which
        // represents our label.  This new frame has our labeltemplate
        // in *its* proto slot.
        Ref label = Ref.call("AddStepView", NS.wabaDrawingArea(), labeltemplate);

        // For good measure, declare the label as dirty so it'll get
        // redrawn.  This is usually necessary, though in this special
        // case, where we start up the app from scratch, it's dirty already.
        // But we're good style programmers, so:
                // Add the template as a stepview of to the wabaDrawingArea
        // view.  This will return us the a view frame which represents
        // our template.
        label.send("Dirty");
        }
    }


