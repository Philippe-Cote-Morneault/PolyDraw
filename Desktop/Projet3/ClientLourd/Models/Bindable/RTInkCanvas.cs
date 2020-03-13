using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Input.StylusPlugIns;

namespace ClientLourd.Models.Bindable
{
    public class RTInkCanvas: InkCanvas
    {
        TransmitStylusEventsPlugin filter = new TransmitStylusEventsPlugin();
        public RTInkCanvas(): base()
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
