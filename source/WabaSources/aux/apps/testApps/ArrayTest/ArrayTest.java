import waba.ui.*;
import newton.*;
import waba.sys.*;

/*
ArrayTest

Tests Array Bugs in the VM
-- Sean
*/

/*
To compile:

javac ArrayTest.java
java wababin.Warp c /f 4 ArrayTest
java wababin.Exegen /h 40 /w 100 /f 4 ArrayTest ArrayTest ArrayTest
open -a PackType ArrayTest.pkg
*/


public class ArrayTest extends MainWindow
    {
    public void onStart()
        {
         byte[] by1 = new byte[0];
         byte[] by2 = {};
         boolean[] bo1 = new boolean[0];
         boolean[] bo2 = {};
         int[] i1 = new int[0];
         int[] i2 = {};
         float[] f1 = new float[0];
         float[] f2 = {};
         Object[] o1 = new Object[0];
         Object[] o2 = {};
        
        int sum = 0;
        sum += by1.length;
        sum += by2.length;
        sum += bo1.length;
        sum += bo2.length;
        sum += i1.length;
        sum += i2.length;
        sum += f1.length;
        sum += f2.length;
        sum += o1.length;
        sum += o2.length;

         byte[] by3 = {1, 2, 3, 4};
         byte[] by4 = new byte[4];
         int[] i4 = new int[4];
        if (Vm.copyArray(by1,0,bo2,0,0)) sum++;
        if (Vm.copyArray(by2,0,i1,0,0)) sum++;
        if (Vm.copyArray(by1,0,by2,0,0)) sum++;
        if (Vm.copyArray(by3,0,by4,0,4)) sum++;
        if (Vm.copyArray(by3,0,i4,0,4)) sum++;
        if (Vm.copyArray(i4,0,by3,0,4)) sum++;
    
        // first we make a frame that points to the protoStaticText proto
        // in its _proto slot.
        Ref labeltemplate = NS.frameWithProto("protoStaticText");
        
        // set its viewBounds slot
        labeltemplate.setSlot("viewBounds", NS.bounds(10,10,100,30));
        
        // set its text value
        labeltemplate.setSlot("text", new Ref(Convert.toString(i4[2])));
        
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


