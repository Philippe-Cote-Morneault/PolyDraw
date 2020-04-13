using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for RTInkCanvas.xaml
    /// </summary>
    public partial class RTInkCanvas : InkCanvas
    {
        public RTInkCanvas(): base()
        {
            InitializeComponent();
        }
    }
}

/*
 using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Input.StylusPlugIns;

namespace ClientLourd.Views.Controls
{
    public class RTInkCanvas : InkCanvas
    {
        TransmitStylusEventsPlugin filter = new TransmitStylusEventsPlugin();
        public RTInkCanvas() : base()
        {
            this.StylusPlugIns.Add(filter);
        }
    }

    class TransmitStylusEventsPlugin : StylusPlugIn
    {
        protected override void OnStylusDown(RawStylusInput rawStylusInput)
        {
            // Call the base class before modifying the data. 
            base.OnStylusDown(rawStylusInput);

            // send drawing attributes of local InkCanvas to peer 
        }

        protected override void OnStylusMove(RawStylusInput rawStylusInput)
        {
            // Call the base class before modifying the data. 
            base.OnStylusMove(rawStylusInput);

            // **** SEND  rawStylusInput.GetStylusPoints()  data over UDP to peer  **** 

        }
    }
}
*/
