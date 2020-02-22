using System.ComponentModel;

namespace ClientLourd.Utilities.Enums
{
    public enum PotraceMode
    {
        [Description("Classic mode (drawing order)")]
        Classic = 0,
        [Description("Random mode")]
        Random = 1,
        [Description("Panoramic mode")]
        Panoramic = 2,
        [Description("Center mode")]
        Center = 3
    }
    
}