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
        public static Stroke AddStroke(this InkCanvas inkCanvas, StrokeInfo strokeInfo, Stroke lastStroke = null)
        {

            if (lastStroke != null && lastStroke.StylusPoints.Count <= 500)
            {
                if ((string) lastStroke.GetPropertyData(GUIDs.ID) == strokeInfo.StrokeID.ToString())
                {
                    AddPointsToStroke(lastStroke, strokeInfo);
                    return lastStroke;
                }
            }
            Stroke stroke = CreateStroke(strokeInfo);
            inkCanvas.Strokes.Add(stroke);
            
            return stroke;
        }

       
        public static void RemoveStroke(this InkCanvas inkCanvas, Guid strokeID)
        {
            FindStrokes(inkCanvas, strokeID).ForEach(s => inkCanvas.Strokes.Remove(s));
        }

        private static List<Stroke> FindStrokes(InkCanvas inkCanvas, Guid strokeID)
        {
            List<Stroke> findstrokes = new List<Stroke>();
            foreach (Stroke stroke in inkCanvas.Strokes)
            {
                if(stroke.GetPropertyData(GUIDs.ID).ToString() == strokeID.ToString())
                {
                    findstrokes.Add(stroke);
                }
            }
            
            return findstrokes;
        }

        private static Stroke CreateStroke(StrokeInfo strokeInfo)
        {
            //Console.WriteLine("Created a new stroke");
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
            //Console.WriteLine("Added points to an existing stroke");
            foreach (StylusPoint sp in strokeInfo.PointCollection)
            {
                stroke.StylusPoints.Add(sp);
            }
        }

        private static double CalculateDistance(StylusPoint point1, StylusPoint point2)
        {
            //  sqrt((X1 - X2)^2 + (Y1 - Y2)^2)
            var xSquare = Math.Pow((point1.X - point2.X), 2);
            var ySquare = Math.Pow((point1.Y - point2.Y), 2);

            return Math.Sqrt(xSquare + ySquare);
        }

        /// <summary>
        /// We create a stroke with points close and then add the stroke to the canvas. 
        /// If the points are far apart, we create a new stroke for those points.
        /// </summary>
        /// <param name="inkCanvas"></param>
        /// <param name="strokeInfo"></param>
        public static void AddStrokePreview(this InkCanvas inkCanvas, StrokeInfo strokeInfo)
        {
            StylusPoint firstPoint = strokeInfo.PointCollection[0];
            StylusPointCollection tmpPoints = new StylusPointCollection();
            tmpPoints.Add(firstPoint);

            for (int i = 0; i < strokeInfo.PointCollection.Count - 1; i++)
            {
                if (CalculateDistance(strokeInfo.PointCollection[i], strokeInfo.PointCollection[i + 1]) < 5)
                {
                    tmpPoints.Add(strokeInfo.PointCollection[i + 1]);
                }
                else
                {
                    inkCanvas.Strokes.Add(CreateStroke(strokeInfo, tmpPoints.Clone()));
                    tmpPoints.Clear();
                    tmpPoints.Add(strokeInfo.PointCollection[i + 1]);
                }
            }

            if (tmpPoints.Count > 0)
            {
                inkCanvas.Strokes.Add(CreateStroke(strokeInfo, tmpPoints));
            }
        }

        /// <summary>
        /// Keeps the stroke attributes (color, height, width, etc.) but uses the param sp as point array instead.
        /// </summary>
        /// <param name="strokeInfo"></param>
        /// <param name="sp"></param>
        /// <returns></returns>
        private static Stroke CreateStroke(StrokeInfo strokeInfo, StylusPointCollection sp)
        {
            Stroke newStroke = new Stroke(sp);
            newStroke.DrawingAttributes.Color = strokeInfo.StrokeColor;
            newStroke.DrawingAttributes.StylusTip = strokeInfo.BrushTip;
            newStroke.DrawingAttributes.Height = strokeInfo.BrushSize;
            newStroke.DrawingAttributes.Width = strokeInfo.BrushSize;
            newStroke.AddPropertyData(GUIDs.ID, strokeInfo.StrokeID.ToString());

            return newStroke;
        }
    }
}
