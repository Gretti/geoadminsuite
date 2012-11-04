<%@ WebHandler Language="C#" Class="mapbuilderProxy" %>

using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.IO;
using System.Net;
using System.Text;

public partial class mapbuilderProxy : IHttpHandler
{

    public bool IsReusable
    {
        get
        {
            return false;
        }
    }

    public void ProcessRequest(HttpContext context)
    {
        HttpResponse Response = context.Response;
        HttpRequest Request = context.Request;
        Response.Write(Redirect(Request["url"],Request));
    }
	
	//warning: limiter les adresses possiblement atteignables
    protected string Redirect(string LaPage,HttpRequest Request)
    {

        HttpWebRequest oWRequest = (HttpWebRequest)WebRequest.Create(LaPage);
        //Response.Write("url: " + LaPage + "\n");
        //post ne marche pas correctement
        if (HttpContext.Current.Request.RequestType == "POST")
        {
            byte[] strRequest = Request.BinaryRead(HttpContext.Current.Request.ContentLength);
            //Response.Write("data: -" + Encoding.UTF8.GetString(strRequest) + "-\n");
            oWRequest.Method = "POST";
            oWRequest.ContentType = "text/xml";//application/x-www-form-urlencoded";//Request.ContentType;
            oWRequest.ContentLength = strRequest.Length;
            // Send the data.
            Stream newStream = oWRequest.GetRequestStream();
            newStream.Write(strRequest, 0, strRequest.Length);
            newStream.Close();
        }
        //la réponse
        StreamReader streamIn = new StreamReader(oWRequest.GetResponse().GetResponseStream());
        string Text = streamIn.ReadToEnd();
        streamIn.Close();
        return Text;
    }
}
