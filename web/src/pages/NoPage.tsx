import { Typography } from "@material-tailwind/react";

export default function NoPage() {
    return (
        <div className="no-page">
            <Typography
                as="a"
                href="#"
                variant="h1"
                className="mr-4 cursor-pointer py-1.5 font-normal">
                    404 ERROR.
            </Typography>
            
            <p>Page not found</p>
        </div>
    );
}