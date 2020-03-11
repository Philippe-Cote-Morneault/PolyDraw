using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Constants;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Ink;
using System.Windows.Input;

namespace ClientLourd.Utilities.Extensions
{
    public static class InkCanvasExtension
    {
        public static void AddStroke(this InkCanvas inkCanvas, StrokeInfo strokeInfo)
        {

            Stroke stroke = FindStroke(inkCanvas, strokeInfo.StrokeID);
            if (stroke != null)
            {
                AddPointsToStroke(stroke, strokeInfo);
                return;
            }

            inkCanvas.Strokes.Add(CreateStroke(strokeInfo));
        }

       
        public static void RemoveStroke(this InkCanvas inkCanvas, Guid strokeID)
        {
            inkCanvas.Strokes.Remove(FindStroke(inkCanvas, strokeID));
        }

        private static Stroke FindStroke(InkCanvas inkCanvas, Guid strokeID)       
        {
            foreach (Stroke stroke in inkCanvas.Strokes)
            {
                if ((string)stroke.GetPropertyData(GUIDs.ID) == strokeID.ToString())
                {
                    return stroke;
                }
            }
            return null;
        }

        private static Stroke CreateStroke(StrokeInfo strokeInfo)
        {
            Console.WriteLine("Created a new stroke");
            Stroke newStroke = new Stroke(strokeInfo.PointCollection);
            newStroke.DrawingAttributes.Color = strokeInfo.StrokeColor;
            newStroke.DrawingAttributes.StylusTip = strokeInfo.BrushTip;
            newStroke.DrawingAttributes.Height = strokeInfo.BrushSize;
            newStroke.DrawingAttributes.Width = strokeInfo.BrushSize;
            newStroke.AddPropertyData(GUIDs.ID, strokeInfo.StrokeID.ToString());

            return newStroke;
        }

        private static void AddPointsToStroke(Stroke stroke, StrokeInfo strokeInfo)
        {
            Console.WriteLine("Added points to an existing stroke");
            foreach (StylusPoint sp in strokeInfo.PointCollection.ToList())
            {
                stroke.StylusPoints.Add(sp);
            }
        }


        /// <summary>
        /// For the preview, the points arent in order. To avoid bugs, we create 1 stroke per point.
        /// </summary>
        /// <param name="inkCanvas"></param>
        /// <param name="strokeInfo"></param>
        public static void AddStrokePreview(this InkCanvas inkCanvas, StrokeInfo strokeInfo)
        {
            foreach (StylusPoint sp in strokeInfo.PointCollection)
            {
                inkCanvas.Strokes.Add(CreateStrokeSinglePoint(sp, strokeInfo));
            }
        }


        private static Stroke CreateStrokeSinglePoint(StylusPoint sp, StrokeInfo strokeInfo)
        {
            StylusPointCollection pc = new StylusPointCollection();
            pc.Add(sp);
            Stroke newStroke = new Stroke(pc);
            newStroke.DrawingAttributes.Color = strokeInfo.StrokeColor;
            newStroke.DrawingAttributes.StylusTip = strokeInfo.BrushTip;
            newStroke.DrawingAttributes.Height = strokeInfo.BrushSize;
            newStroke.DrawingAttributes.Width = strokeInfo.BrushSize;
            newStroke.AddPropertyData(GUIDs.ID, strokeInfo.StrokeID.ToString());

            return newStroke;
        }

    }
}
