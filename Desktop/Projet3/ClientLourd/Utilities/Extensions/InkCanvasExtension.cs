﻿using ClientLourd.Models.NonBindable;
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
            /*foreach (StylusPoint sp in strokeInfo.PointCollection.ToList())
            {
                stroke.StylusPoints.Add(sp);
            }*/
            for (int i = 0; i < 25; i++)
            {
                stroke.StylusPoints.Add(new StylusPoint(i, i));
            }

        }

    }
}
