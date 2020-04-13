using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reflection;

namespace ClientLourd.Services.EnumService
{
    public static class EnumManager
    {
        public static T GetEnumFromDescription<T>(this string description)
        {
            var type = typeof(T);
            if (!type.IsEnum) throw new InvalidOperationException();
            foreach (var field in type.GetFields())
            {
                var attribute = Attribute.GetCustomAttribute(field,
                    typeof(DescriptionAttribute)) as DescriptionAttribute;
                if (attribute != null)
                {
                    if (attribute.Description == description)
                        return (T) field.GetValue(null);
                }
                else
                {
                    if (field.Name == description)
                        return (T) field.GetValue(null);
                }
            }

            throw new ArgumentException("Not found.", "description");
        }

        public static List<string> GetAllDescriptions<T>()
        {
            List<string> descriptions = new List<string>();
            foreach (Enum e in Enum.GetValues(typeof(T)))
            {
                descriptions.Add(e.GetDescription());
            }

            return descriptions;
        }

        public static string GetDescription(this Enum value)
        {
            FieldInfo field = value.GetType().GetField(value.ToString());

            DescriptionAttribute attribute
                = Attribute.GetCustomAttribute(field, typeof(DescriptionAttribute))
                    as DescriptionAttribute;

            return attribute == null ? value.ToString() : attribute.Description;
        }
    }
}