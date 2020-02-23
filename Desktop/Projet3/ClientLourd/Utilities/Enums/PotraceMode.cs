using System.ComponentModel;

namespace ClientLourd.Utilities.Enums
{
    public enum PotraceMode
    {
        [Description("Classic mode (drawing order)")]
        Classic = 0,
        [Description("Random mode")]
        Random = 1,
        [Description("Right to left")]
        RightToLeft = 2,
        [Description("Left to right")]
        LeftToRight = 3,
        [Description("Top to bottom")]
        TopToBottom = 4,
        [Description("Bottom to top")]
        BottomToTop = 5,
        [Description("Inside to outside")]
        InsideToOutside = 6,
        [Description("Outside to inside")]
        OutsideToInside = 7,
    }
    
}