<html><head><title>Overview</title></head>
<body>

<p><b><font color=Red size=4>See Important Note Below.</font></b>

<p><b>Note</b>: Waba does not use the
same <b>java.lang.Object</b>, <b>java.lang.String</b>, and 
<b>java.lang.StringBuffer</b> classes as Java uses.  Instead,
Waba has versions of these classes which have just a very
small number of methods.  The documentation for these classes is at:
<ol>
<li><a href="java/lang/Object.html">java.lang.Object</a>
<li><a href="java/lang/String.html">java.lang.String</a>
<li><a href="java/lang/StringBuffer.html">java.lang.StringBuffer</a>
</ol>

<p>This documentation also includes the
<b>waba.applet.*</b> classes.  These classes are for
running your Waba application on your PC or Mac, rather than the
Newton.  The only thing you really need to know about these classes
is that you can fire up a Waba applet as follows.  If your applet's
MainWindow subclass is called Foo, then you can say
<b>java waba.applet.Applet Foo [optional /w &lt;width&gt;] [optional /h &lt;height&gt;]</b>

</body></html>
